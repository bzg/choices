# Copyright (c) 2019 DINSIC, Bastien Guerry <bastien.guerry@data.gouv.fr>
# SPDX-License-Identifier: EPL-2.0
# License-Filename: LICENSES/EPL-2.0.txt

FROM java:8-alpine
ENV CHOICES_PORT ${CHOICES_PORT}
ADD target/choices-standalone.jar /choices/choices-standalone.jar
CMD ["java", "-jar", "/choices/choices-standalone.jar"]
