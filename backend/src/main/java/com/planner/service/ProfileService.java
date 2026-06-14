package com.planner.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.planner.common.BusinessException;
import com.planner.dto.request.ProfileAiUpdateRequest;
import com.planner.dto.request.ProfileDocumentUpdateRequest;
import com.planner.dto.request.ProfileMdGenerateRequest;
import com.planner.dto.request.SkillUpdateRequest;
import com.planner.dto.response.ProfileAiUpdateResponse;
import com.planner.dto.response.ProfileMdResponse;
import com.planner.entity.LearningRecord;
import com.planner.entity.ProfileChangeHistory;
import com.planner.entity.SkillProfile;
import com.planner.mapper.LearningRecordMapper;
import com.planner.mapper.ProfileChangeHistoryMapper;
import com.planner.mapper.ProfileDocumentMapper;
import com.planner.mapper.SkillProfileMapper;
import com.planner.entity.ProfileDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final SkillProfileMapper profileMapper;
    private final ProfileChangeHistoryMapper historyMapper;
    private final LearningRecordMapper learningMapper;
    private final ProfileDocumentMapper profileDocumentMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate(buildFactory());

    private static SimpleClientHttpRequestFactory buildFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(120000);
        return factory;
    }

    @Value("${minimax.api-key}")
    private String minimaxApiKey;

    @Value("${minimax.base-url}")
    private String minimaxBaseUrl;

    @Value("${minimax.model}")
    private String minimaxModel;

    public List<SkillProfile> getProfile(Long userId) {
        return profileMapper.selectList(new LambdaQueryWrapper<SkillProfile>()
                .eq(SkillProfile::getUserId, userId)
                .orderByAsc(SkillProfile::getCategory, SkillProfile::getSkillName));
    }

    @Transactional
    public SkillProfile updateSkill(Long userId, Long skillId, SkillUpdateRequest req) {
        SkillProfile skill = profileMapper.selectById(skillId);
        if (skill == null || !skill.getUserId().equals(userId)) {
            throw new BusinessException("技能不存在");
        }
        if (req.getCategory() != null) skill.setCategory(req.getCategory());
        if (req.getSkillName() != null) skill.setSkillName(req.getSkillName());
        if (req.getLevel() != null) skill.setLevel(req.getLevel());
        if (req.getKeywords() != null) skill.setKeywords(req.getKeywords());
        if (req.getNotes() != null) skill.setNotes(req.getNotes());
        skill.setUpdatedAt(LocalDateTime.now());
        profileMapper.updateById(skill);
        return skill;
    }

    @Transactional
    public SkillProfile addSkill(Long userId, SkillUpdateRequest req) {
        SkillProfile existing = profileMapper.selectOne(
                new LambdaQueryWrapper<SkillProfile>()
                        .eq(SkillProfile::getUserId, userId)
                        .eq(SkillProfile::getCategory, req.getCategory())
                        .eq(SkillProfile::getSkillName, req.getSkillName()));
        if (existing != null) {
            throw new BusinessException("技能已存在");
        }
        SkillProfile skill = new SkillProfile();
        skill.setUserId(userId);
        skill.setCategory(req.getCategory());
        skill.setSkillName(req.getSkillName());
        skill.setLevel(req.getLevel() != null ? req.getLevel() : "C");
        skill.setKeywords(req.getKeywords());
        skill.setNotes(req.getNotes());
        skill.setUpdatedAt(LocalDateTime.now());
        profileMapper.insert(skill);
        saveHistory(userId, skill.getSkillName(), null, skill.getLevel(), "new", "手动新增技能");
        return skill;
    }

    public List<ProfileChangeHistory> getHistory(Long userId) {
        return historyMapper.selectList(new LambdaQueryWrapper<ProfileChangeHistory>()
                .eq(ProfileChangeHistory::getUserId, userId)
                .orderByDesc(ProfileChangeHistory::getCreatedAt));
    }

    public ProfileDocument getProfileDocument(Long userId) {
        return profileDocumentMapper.selectOne(
                new LambdaQueryWrapper<ProfileDocument>().eq(ProfileDocument::getUserId, userId));
    }

    @Transactional
    public ProfileMdResponse saveOrUpdateProfileDocument(Long userId, ProfileDocumentUpdateRequest req) {
        String markdown;
        String sourceMd = req.getSourceMd();
        List<String> suggested = new ArrayList<>();

        boolean regenerate = req.getRegenerate() != null && req.getRegenerate();
        boolean isFirstGenerate = sourceMd == null || sourceMd.trim().isEmpty();
        if (regenerate && isFirstGenerate) {
            sourceMd = "";
        }
        if (regenerate) {
            List<SkillProfile> profile = getProfile(userId);
            List<LearningRecord> records = new ArrayList<>();
            if (req.getIncludeLearning() == null || req.getIncludeLearning()) {
                records = learningMapper.selectList(
                        new LambdaQueryWrapper<LearningRecord>()
                                .eq(LearningRecord::getUserId, userId)
                                .ge(LearningRecord::getCreatedAt, LocalDateTime.now().minusDays(30))
                                .orderByAsc(LearningRecord::getCreatedAt));
            }
            String prompt = buildMdProfilePrompt(profile, records, sourceMd);
            markdown = callLlmForMd(prompt);
            suggested = extractSuggestedSkills(markdown);
        } else {
            markdown = req.getMarkdown();
            if (markdown == null) {
                ProfileDocument existing = getProfileDocument(userId);
                markdown = existing != null ? existing.getMarkdown() : "";
            }
        }

        ProfileDocument doc = getProfileDocument(userId);
        LocalDateTime now = LocalDateTime.now();
        if (doc == null) {
            doc = new ProfileDocument();
            doc.setUserId(userId);
            doc.setVersion(1);
            doc.setCreatedAt(now);
        } else {
            doc.setVersion((doc.getVersion() == null ? 0 : doc.getVersion()) + 1);
        }
        doc.setMarkdown(markdown);
        doc.setSourceMd(sourceMd);
        if (!suggested.isEmpty()) {
            try {
                doc.setSuggestedSkills(objectMapper.writeValueAsString(suggested));
            } catch (Exception ignored) {}
        }
        doc.setUpdatedAt(now);
        if (doc.getId() == null) {
            profileDocumentMapper.insert(doc);
        } else {
            profileDocumentMapper.updateById(doc);
        }

        ProfileMdResponse response = new ProfileMdResponse();
        response.setMarkdown(markdown);
        response.setSuggestedSkills(suggested);
        return response;
    }

    @Transactional
    public ProfileAiUpdateResponse aiUpdate(Long userId, ProfileAiUpdateRequest req) {
        LocalDateTime since = req.getSince() != null ? req.getSince() : LocalDateTime.now().minusDays(7);
        List<LearningRecord> records = learningMapper.selectList(
                new LambdaQueryWrapper<LearningRecord>()
                        .eq(LearningRecord::getUserId, userId)
                        .ge(LearningRecord::getCreatedAt, since)
                        .orderByAsc(LearningRecord::getCreatedAt));
        List<SkillProfile> profile = getProfile(userId);
        String prompt = buildAiUpdatePrompt(profile, records);
        ProfileAiUpdateResponse response = callLlm(prompt);
        for (ProfileAiUpdateResponse.SkillUpdate u : response.getUpdates()) {
            String newLevel = normalizeLevel(u.getNewLevel());
            if (newLevel == null) continue;
            SkillProfile existing = profileMapper.selectOne(
                    new LambdaQueryWrapper<SkillProfile>()
                            .eq(SkillProfile::getUserId, userId)
                            .eq(SkillProfile::getSkillName, u.getSkill()));
            if (existing != null) {
                String oldLevel = existing.getLevel();
                existing.setLevel(newLevel);
                existing.setUpdatedAt(LocalDateTime.now());
                profileMapper.updateById(existing);
                saveHistory(userId, u.getSkill(), oldLevel, newLevel, "ai", u.getReason());
            }
        }
        return response;
    }

    private String normalizeLevel(String level) {
        if (level == null) return null;
        String s = level.trim().toUpperCase();
        Set<String> valid = Set.of("S", "A", "B", "C", "D");
        if (valid.contains(s)) return s;
        if (s.startsWith("S") || s.contains("S+") || s.contains("S-")) return "S";
        if (s.startsWith("A+")) return "S";
        if (s.startsWith("A") || s.contains("A-")) return "A";
        if (s.startsWith("B+")) return "A";
        if (s.startsWith("B") || s.contains("B-")) return "B";
        if (s.startsWith("C+")) return "B";
        if (s.startsWith("C") || s.contains("C-")) return "C";
        if (s.startsWith("D+")) return "C";
        if (s.startsWith("D") || s.contains("D-")) return "D";
        return null;
    }

    private String buildAiUpdatePrompt(List<SkillProfile> profile, List<LearningRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的技术教练。请分析用户最近的学习记录，更新其技能画像。\n\n");
        sb.append("## 当前技能画像\n");
        sb.append("[");
        for (int i = 0; i < profile.size(); i++) {
            SkillProfile p = profile.get(i);
            sb.append(String.format("{\"category\": \"%s\", \"skill\": \"%s\", \"level\": \"%s\", \"notes\": \"%s\"}",
                    p.getCategory(), p.getSkillName(), p.getLevel(), p.getNotes() != null ? p.getNotes() : ""));
            if (i < profile.size() - 1) sb.append(",");
        }
        sb.append("]\n\n## 最近学习记录\n[");
        for (int i = 0; i < records.size(); i++) {
            LearningRecord r = records.get(i);
            sb.append(String.format("{\"date\": \"%s\", \"title\": \"%s\", \"content\": \"%s\", \"duration\": %d}",
                    r.getCreatedAt().toLocalDate(), r.getTitle(),
                    r.getContent() != null ? r.getContent().substring(0, Math.min(200, r.getContent().length())) : "",
                    r.getDurationMinutes() != null ? r.getDurationMinutes() : 0));
            if (i < records.size() - 1) sb.append(",");
        }
        sb.append("]\n\n请返回JSON（严格按格式，返回的updates必须来自学习记录的直接支撑，不要过度提升）：\n");
        sb.append("{\"updates\": [{\"skill\": \"技能名\", \"oldLevel\": \"B\", \"newLevel\": \"B+\", \"reason\": \"原因\"}], \"summary\": \"本周技术提升...\"}");
        return sb.toString();
    }

    private ProfileAiUpdateResponse callLlm(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", minimaxApiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", new Object[]{
                    Map.of("role", "user", "content", prompt)
            });
            requestBody.put("max_tokens", 2048);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String url = minimaxBaseUrl + "/v1/messages?model=" + minimaxModel;

            String response = restTemplate.postForObject(url, entity, String.class);

            Map<String, Object> respMap = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> content = (List<Map<String, Object>>) respMap.get("content");
            if (content == null || content.isEmpty()) {
                return fallbackResponse();
            }
            StringBuilder textBuilder = new StringBuilder();
            for (Map<String, Object> block : content) {
                if (block.containsKey("text")) {
                    textBuilder.append(block.get("text"));
                }
            }
            return parseLlmResponse(textBuilder.toString());
        } catch (Exception e) {
            return fallbackResponse();
        }
    }

    private ProfileAiUpdateResponse parseLlmResponse(String content) {
        try {
            content = content.trim();
            if (content.contains("```json")) {
                content = content.substring(content.indexOf("```json") + 7);
            }
            if (content.contains("```")) {
                content = content.substring(0, content.indexOf("```"));
            }
            content = content.trim();

            ProfileAiUpdateResponse response = objectMapper.readValue(content, ProfileAiUpdateResponse.class);
            return response;
        } catch (Exception e) {
            return fallbackResponse();
        }
    }

    private ProfileAiUpdateResponse fallbackResponse() {
        ProfileAiUpdateResponse response = new ProfileAiUpdateResponse();
        response.setUpdates(new ArrayList<>());
        response.setSummary("AI 更新功能需要配置 LLM API Key");
        return response;
    }

    public ProfileMdResponse generateMdProfile(Long userId, ProfileMdGenerateRequest req) {
        List<SkillProfile> profile = getProfile(userId);
        List<LearningRecord> records = new ArrayList<>();
        if (req.getIncludeLearning() != null && req.getIncludeLearning()) {
            records = learningMapper.selectList(
                    new LambdaQueryWrapper<LearningRecord>()
                            .eq(LearningRecord::getUserId, userId)
                            .ge(LearningRecord::getCreatedAt, LocalDateTime.now().minusDays(30))
                            .orderByAsc(LearningRecord::getCreatedAt));
        }
        String prompt = buildMdProfilePrompt(profile, records, req.getMdContent());
        String md = callLlmForMd(prompt);
        List<String> suggested = extractSuggestedSkills(md);
        ProfileMdResponse response = new ProfileMdResponse();
        response.setMarkdown(md);
        response.setSuggestedSkills(suggested);
        return response;
    }

    private String buildMdProfilePrompt(List<SkillProfile> profile, List<LearningRecord> records, String uploadedMd) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位资深技术专家兼职业规划顾问。你的任务是基于【用户上传的文档】（最权威依据）、\n");
        sb.append("结合【当前技能画像】和【最近学习记录】，生成一份**结构清晰、内容精炼、忠实于用户原文**的\n");
        sb.append("个人技术画像（Markdown 格式）。\n\n");

        sb.append("## ⚠️ 重要原则\n");
        sb.append("1. **优先使用上传文档** 中的事实：用户对自己技术的自评、项目经历、学习计划要原样采纳\n");
        sb.append("2. 用户在文档中已经给出明确评级（如\"熟练/掌握/了解\"），请**直接映射**到 S/A/B/C/D：\n");
        sb.append("   - 熟练/精通 → A 或 S；掌握 → B；了解 → C；薄弱/入门 → D\n");
        sb.append("3. 不要凭空编造用户没提到的项目或技术\n");
        sb.append("4. 文档中提到的【核心竞争力】【P0/P1/P2 优先级】【能力矩阵】等结构，请**直接吸收**到对应章节\n\n");

        sb.append("## 用户上传文档（作为画像主要基础）\n");
        if (uploadedMd != null && !uploadedMd.trim().isEmpty()) {
            String trimmed = uploadedMd.length() > 12000 ? uploadedMd.substring(0, 12000) + "\n...(文档已截断)..." : uploadedMd;
            sb.append("```\n").append(trimmed).append("\n```\n\n");
        } else {
            sb.append("（未提供，请基于系统中的技能画像和学习记录生成）\n\n");
        }

        sb.append("## 当前技能画像（系统记录）\n");
        if (profile.isEmpty()) {
            sb.append("（暂无）\n\n");
        } else {
            for (SkillProfile p : profile) {
                sb.append(String.format("- %s / %s / %s：%s\n",
                        p.getCategory(), p.getSkillName(), p.getLevel(),
                        p.getNotes() != null ? p.getNotes() : ""));
            }
            sb.append("\n");
        }

        sb.append("## 最近学习记录（最多 30 天）\n");
        if (records.isEmpty()) {
            sb.append("（暂无）\n\n");
        } else {
            for (LearningRecord r : records) {
                String content = r.getContent() != null ? r.getContent() : "";
                content = content.length() > 150 ? content.substring(0, 150) + "..." : content;
                sb.append(String.format("- [%s] %s（%d分钟）：%s\n",
                        r.getCreatedAt().toLocalDate(), r.getTitle(),
                        r.getDurationMinutes() != null ? r.getDurationMinutes() : 0, content));
            }
            sb.append("\n");
        }

        sb.append("## 输出要求\n");
        sb.append("请严格按以下 Markdown 结构输出（**不要任何额外说明、不要代码块包裹整个输出**）：\n\n");
        sb.append("# 个人技术画像\n\n");
        sb.append("## 1. 总体概括\n");
        sb.append("（2-3 段：当前技术方向、核心定位、整体水平评估、近期发展态势）\n\n");
        sb.append("## 2. 个人项目沉淀\n");
        sb.append("（按项目分小节，列出每个项目的：角色、技术栈、关键成果——保留具体数据如\"耗时-30%\"、\"消重50%\"等）\n");
        sb.append("### 项目 A：项目名（角色）\n");
        sb.append("- **简介**：xxx\n");
        sb.append("- **技术栈**：xxx\n");
        sb.append("- **关键成果**：3-5 条带数据的亮点\n");
        sb.append("### 项目 B：项目名（角色）\n");
        sb.append("（如有更多项目，依次列出）\n\n");
        sb.append("## 3. 掌握的能力\n");
        sb.append("（按一级分类组织，**忠实于用户文档自评**——熟练→A，掌握→B，了解→C）\n");
        sb.append("### Java 后端\n");
        sb.append("- **Spring Boot (A)**：xxx（1 句证据）\n");
        sb.append("- **Spring AI (A)**：xxx\n");
        sb.append("### 中间件与基础设施\n");
        sb.append("- **MySQL (A)**：xxx\n");
        sb.append("- **Redis (A)**：xxx\n");
        sb.append("### AI / LLM 工程化\n");
        sb.append("- **RAG (A)**：xxx\n");
        sb.append("- **Tool Calling (A)**：xxx\n");
        sb.append("### 高并发与性能\n");
        sb.append("- **二级缓存 (A)**：xxx\n");
        sb.append("### 前端 / 大数据 / 其他\n");
        sb.append("- **Vue (B)**：xxx\n");
        sb.append("（没有的分类可省略）\n\n");
        sb.append("## 4. 未掌握 / 短板\n");
        sb.append("（基于文档\"需要补齐\"部分 + LLM 判断，分 P0/P1/P2 优先级列出，每项带具体差距描述）\n");
        sb.append("### P0 — 必须补齐（影响职级跃迁）\n");
        sb.append("- **能力名**：现状差距 → 补齐路径\n");
        sb.append("### P1 — 应该补齐（扩展边界）\n");
        sb.append("- **能力名**：现状差距 → 补齐路径\n");
        sb.append("### P2 — 了解即可（加分项）\n");
        sb.append("- **能力名**：了解方向\n\n");
        sb.append("## 5. 短板学习建议（针对个人）\n");
        sb.append("（针对上面 P0/P1 短板的**具体可执行**学习计划）\n");
        sb.append("- **第 1 个月**：xxx（学什么、产出什么）\n");
        sb.append("- **第 2-3 个月**：xxx\n");
        sb.append("- **第 4-6 个月**：xxx\n");
        sb.append("- **学习资源**：书/博客/视频清单（3-5 个）\n\n");
        sb.append("## 6. 计算机行业学习建议（针对整个行业）\n");
        sb.append("（从职业发展、技术趋势、能力模型三个维度给出建议）\n");
        sb.append("### 6.1 职业发展建议\n");
        sb.append("- （P5→P6→P7 不同阶段的关键能力、跳槽/晋升时机、沟通与影响力等）\n\n");
        sb.append("### 6.2 技术趋势建议（2026 年）\n");
        sb.append("- （AI/Agent/MCP 协议、RAG 演进、向量库、Serverless、可观测性等需要关注的方向）\n\n");
        sb.append("### 6.3 通用能力建议\n");
        sb.append("- （技术写作、方案设计、代码 review、业务理解、影响力建设）\n\n");
        sb.append("---\n");
        sb.append("**评级说明**: S=精通/业界专家 | A=熟练/能独立负责 | B=掌握/能上手项目 | C=了解/基本使用 | D=入门\n");
        return sb.toString();
    }

    private String callLlmForMd(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", minimaxApiKey);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", new Object[]{
                    Map.of("role", "user", "content", prompt)
            });
            requestBody.put("max_tokens", 4096);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String url = minimaxBaseUrl + "/v1/messages?model=" + minimaxModel;

            String response = restTemplate.postForObject(url, entity, String.class);

            Map<String, Object> respMap = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> content = (List<Map<String, Object>>) respMap.get("content");
            if (content == null || content.isEmpty()) {
                return "AI 生成失败：响应为空";
            }
            StringBuilder textBuilder = new StringBuilder();
            for (Map<String, Object> block : content) {
                if (block.containsKey("text")) {
                    textBuilder.append(block.get("text"));
                }
            }
            return textBuilder.toString();
        } catch (Exception e) {
            return "AI 生成失败：" + e.getMessage();
        }
    }

    private List<String> extractSuggestedSkills(String md) {
        List<String> result = new ArrayList<>();
        if (md == null) return result;
        String[] lines = md.split("\n");
        boolean inSection = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("## 4.") || trimmed.contains("未掌握") || trimmed.contains("短板")) {
                inSection = true;
                continue;
            }
            if (inSection && trimmed.startsWith("## ") && !trimmed.contains("未掌握") && !trimmed.contains("短板")) {
                break;
            }
            if (inSection && (trimmed.startsWith("- ") || trimmed.startsWith("### "))) {
                if (trimmed.startsWith("### ")) continue;
                String item = trimmed.substring(2);
                int colon = item.indexOf("：");
                if (colon == -1) colon = item.indexOf(":");
                if (colon > 0) {
                    String name = item.substring(0, colon).trim();
                    name = name.replaceAll("\\*\\*", "").replaceAll("\\([^)]*\\)", "").trim();
                    if (!name.isEmpty()) result.add(name);
                }
            }
        }
        return result;
    }

    private void saveHistory(Long userId, String skillName, String oldLevel, String newLevel, String changeType, String reason) {
        ProfileChangeHistory h = new ProfileChangeHistory();
        h.setUserId(userId);
        h.setSkillName(skillName);
        h.setOldLevel(oldLevel);
        h.setNewLevel(newLevel);
        h.setChangeType(changeType);
        h.setReason(reason);
        h.setCreatedAt(LocalDateTime.now());
        historyMapper.insert(h);
    }
}