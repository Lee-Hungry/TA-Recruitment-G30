# TA-Recruitment-G30
This is the group project assignment for EBU6304 Software Engineering Course Group 30.

# Group Name-List

- GitHub Account: QMID(Lead/Member)
- qrsikno2: 190898878(Support TA)
- Lee-Hungry: 231220507(Lead)
- Hanhan-2005: 231222615(Member)
- MobiusRita: 231222121(Member)
- jersey945: 231222567(Member)
- Lq999-png: 231220068(Member)
- ZhixianDong: 231220703(Member)

# Sprint 2 Responsibilities

- Lee-Hungry (Li Wenxiang, lead): US-013 Admin workload summary, settings wiring, admin dashboard integration, release cleanup and merge coordination.
- Hanhan-2005 (Liu Xinran): US-009 TA job application submission flow, application CSV repository, launcher wiring, duplicate submission guard.
- MobiusRita (Han Zhiye): US-010 TA application status workspace, dashboard summary updates, TA-side status copy and UI regression coverage.
- jersey945 (Li Zhexi): US-011 MO applicant browsing flow, applicant projection from TA profiles, MO applicant detail workspace.
- Lq999-png (Liu Xintong): US-008 TA CV upload and profile persistence, TA profile CSV compatibility, dashboard CV entry point.
- ZhixianDong (Dong Zhixian): US-012 MO accept/reject workflow, application status update path, dedicated MO session storage.

# Build & Run

Use Maven to package and run the Swing app:

```bash
mvn clean package
mvn -q exec:java -Dexec.mainClass="com.group30.tarecruitment.AppLauncher"
```
