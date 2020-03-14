package io.infinite.orbit.components

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.infinite.blackbox.BlackBox
import io.infinite.orbit.entities.Template
import io.infinite.orbit.model.TemplateSelectionData
import io.infinite.orbit.other.OrbitException
import io.infinite.orbit.other.TemplateTypes
import io.infinite.orbit.repositories.TemplateRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@ToString(includeNames = true, includeFields = true)
@BlackBox
@Slf4j
@Component
class TemplateSelector {

    @Autowired
    TemplateRepository templateRepository

    Template priorityOne(Set<Template> templates, TemplateSelectionData templateSelectionData) {
        return templates.find {
            it.application == templateSelectionData.application &&
                    it.language == templateSelectionData.language &&
                    it.channel == templateSelectionData.channel
        }
    }

    Template priorityTwo(Set<Template> templates, TemplateSelectionData templateSelectionData) {
        return templates.find {
            it.application == templateSelectionData.application &&
                    it.channel == templateSelectionData.channel
        }
    }

    Template priorityThree(Set<Template> templates, TemplateSelectionData templateSelectionData) {
        return templates.find {
            it.environment == templateSelectionData.environment &&
                    it.language == templateSelectionData.language &&
                    it.channel == templateSelectionData.channel
        }
    }

    Template priorityFour(Set<Template> templates, TemplateSelectionData templateSelectionData) {
        return templates.find {
            it.environment == templateSelectionData.environment &&
                    it.channel == templateSelectionData.channel
        }
    }

    Template priorityFive(Set<Template> templates, TemplateSelectionData templateSelectionData) {
        return templates.find {
            it.language == templateSelectionData.language &&
                    it.channel == templateSelectionData.channel
        }
    }

    Template prioritySix(Set<Template> templates, TemplateSelectionData templateSelectionData) {
        return templates.find {
            it.channel == templateSelectionData.channel
        }
    }

    Template findTemplate(Set<Template> templates, TemplateSelectionData templateSelectionData) {
        Template result = [
                priorityOne(templates, templateSelectionData),
                priorityTwo(templates, templateSelectionData),
                priorityThree(templates, templateSelectionData),
                priorityFour(templates, templateSelectionData),
                priorityFive(templates, templateSelectionData),
                prioritySix(templates, templateSelectionData)
        ].find { it != null }
        if (result == null) {
            throw new OrbitException("Template not found: $templateSelectionData")
        }
        return result
    }

    String executeTemplate(TemplateSelectionData templateSelectionData, String partnerId, TemplateTypes templateType, Map<String, String> templateValues) {
        Set<Template> templates = templateRepository.findByNameAndPartnerIdAndType(
                templateSelectionData.templateName,
                partnerId,
                templateType.value()
        )
        Template template = findTemplate(templates, templateSelectionData)
        String result = template.text
        templateValues.each { k, v ->
            result = result.replace("\${" + k + "}", v)
        }
        return result
    }

}
