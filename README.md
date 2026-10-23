# CST 438 Project 02: MealPlan and Recipe (API REPO)

[Application Repo](https://github.com/Solian21/CST438_P2_Andrioid_App)<br>

Our API is an API that allows users to create recipes, ingredients, and meal plans. A user can use premade recipes or ingredients and add ingredients to their recipes. A user can make user specific weekly meal plans with meal plan entries that have recipes in them.<br>
## Our Team
#### DOYOUNG YANG - Yooze0x4c8

[Issues](https://github.com/Solian21/CST438_P2_API/issues?q=is%3Aissue+state%3Aclosed+assignee%3AYooze0x4c8)<br>
[Pull Requests](https://github.com/Solian21/CST438_P2_API/pulls?q=is%3Apr+state%3Aclosed+author%3AYooze0x4c8)<br><br>
**Worked on**<br>
- Project setup: Created the Spring Boot skeleton and wrote the OpenAPI (api.yaml) contract
- DevOps: Added a Dockerfile, docker-compose for local dev, and GitHub Actions CI
- Quality gates: Required 70% service-layer test coverage with JaCoCo and set up auto reviewers
- Deployment: Published the Docker image to GHCR on merge to main
- Meal Plan API: Built CRUD routes for meal plans and their entries, plus Swagger UI docs


#### Lesly Villanueva - leslyVillanueva1020

[Issues](https://github.com/Solian21/CST438_P2_API/issues?q=is%3Aissue+state%3Aclosed+assignee%3AleslyVillanueva1020)<br>
[Pull Requests](https://github.com/Solian21/CST438_P2_API/pulls?q=is%3Apr+state%3Aclosed+author%3AleslyVillanueva1020)<br><br>
**Worked on**<br>
- Built the the User, Ingredient, and Recipe entities
    - Including tables, constraints, and relationships in neon
- Ingredient API has pagination browsing, duplicate name validation, and service tests
- Developed Recipe API functionality, pagination, sorting, ingredient association, ownership checks, and HTTP responses

#### Carlos Solian - Solian21

[Issues](https://github.com/Solian21/CST438_P2_API/issues?q=is%3Aissue+state%3Aclosed+assignee%3ASolian21)<br>
[Issues on App Repo](https://github.com/Solian21/CST438_P2_Andrioid_App/issues?q=is%3Aissue+state%3Aclosed+assignee%3ASolian21)<br>
[Pull Requests](https://github.com/Solian21/CST438_P2_API/pulls?q=is%3Apr+state%3Aclosed+author%3ASolian21)<br>
[Pull Requests on App Repo](https://github.com/Solian21/CST438_P2_Andrioid_App/pulls?q=is%3Apr+state%3Aclosed)<br><br>
**Worked on**<br>
- Built the Android App interface 
  - Created the login and sign up screens
  - Designed the home screen and navigation menu
  - Added the Recipe Screens
- Added Google OAuth to both the Back end and the Android App


#### Annabelle Baltes - annabelleb20

[Issues](https://github.com/Solian21/CST438_P2_API/issues?q=is%3Aissue+state%3Aclosed+assignee%3Aannabelleb20)<br>
[Pull Requests](https://github.com/Solian21/CST438_P2_API/pulls?q=is%3Apr+state%3Aclosed+author%3Aannabelleb20)<br><br>
**Worked on**<br>
- Helped set up the online database
- Built the Meal Plan and Meal Plan Entry database entities
- Worked on the User GET, PATCH, and DELETE API endpoints
- Started the work on the API Springboot security and error handling.


## How To Set Up Locally
Pull the repo into Intellij or a similar IDE, then go into the terminal and ensure you are in the api directory.<br>
Then run `./gradlew build`

[Local Swagger URL](http://localhost:8080/swagger-ui/index.html#/)

## Live URLs
[Render URL](https://cst438-p2-api.onrender.com)<br>
[Swagger URL](https://cst438-p2-api.onrender.com/swagger-ui/index.html)