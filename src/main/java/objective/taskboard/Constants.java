package objective.taskboard;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

public abstract class Constants {
    
    public static final String SCHEMA_MAD = "mad";
    public static final String SCHEMA_JIRA = "jira";
    
    public static final String CUSTOMFIELD_TAMANHO = "customfield_18520";
    public static final String CUSTOMFIELD_CLASSE_DE_SERVICO = "customfield_18522";
    public static final String CUSTOMFIELD_TEAM = "customfield_10463";
    public static final String CUSTOMFIELD_IMPEDIDO = "customfield_18521";
    public static final String CUSTOMFIELD_SUB_RESPONSAVEIS = "customfield_18523";
    public static final String CUSTOMFIELD_AMBIENTE_CLIENTE = "customfield_10013";
    public static final String CUSTOMFIELD_DETECTADO_POR = "customfield_10534";
    public static final String CUSTOMFIELD_ASSUNTO_COPEL = "customfield_10290";
    public static final String CUSTOMFIELD_ESTIMATIVA = "customfield_10170";

    public static final String LINK_REQUIREMENT_NAME = "Depends";
    public static final String LINK_DEMANDA_NAME = "Demand";

    public static final int ISSUETYPE_ID_TASK = 9999999;
    public static final int ISSUETYPE_ID_OS = 11701;
    public static final int ISSUETYPE_ID_BUG = 1;

    public static final String ISSUETYPE_DEMANDA = "DEMAND";

    public static final int STATUS_CATEGORY_ID_IN_PROGRESS = 4;

    public static final List<String> TRANSITIONS_DONE = newArrayList("Done");
    public static final String TRANSITION_CANCELAR = "Won't Do";

    public static final String RESOLUTION_DONE = "Done";
    public static final String RESOLUTION_CANCELED = "Won't Do";

    public static final String IMPEDED_ID_OPTION = "19315";
}
