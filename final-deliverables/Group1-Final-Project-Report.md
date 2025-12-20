# SOFTWARE DEVELOPMENT AS A TEAM

# SWE574

## CONNECT THE DOTS
Group Members:
* BATUHAN CÖMERT
* ESRA NUR ÖZÜM
* GAMZE GÜNERİ
* YASEMİN TANGÜL
* MEHMET YUSUF BAYAM

Date: 20/12/2025

Repository: https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots

Tag Version : V0.9

Deployment: 13.60.88.202:3000

---

# Table of Content

* [Honor Codes](#honor-codes)
* [List of Deliverables](#list-of-deliverables)
* [Application Videos](#application-videos)
* [Demo User Information](#demo-user-information)
* [Project Overview (Executive Summary)](#project-overview-executive-summary)
    * [Target according to SRS](#target-according-to-srs)
    * [What is actually delivered](#what-is-actually-delivered)
    * [Missing/partial items according to SRS](#missingpartial-items-according-to-srs)
* [Software Requirements Specifications](#software-requirements-specifications)
    * [Purpose](#purpose)
    * [Scope of The Project](#scope-of-the-project)
    * [Glossary](#glossary)
    * [1. Functional Requirements](#1-functional-requirements)
    * [2. Non-functional Requirements](#2-non-functional-requirements)
    * [3. System Architecture](#3-system-architecture)
* [Status of the Project](#status-of-the-project)
* [Dockerization Status](#dockerization-status)
    * [Container Architecture Overview](#container-architecture-overview)
    * [Multi-Environment Configuration](#multi-environment-configuration)
* [Status of the Deployment](#status-of-the-deployment)
    * [Deployment Architecture](#deployment-architecture)
    * [Deployment Environments](#deployment-environments)
    * [Automated Deployment](#automated-deployment)
    * [Deployment Process](#deployment-process)
    * [Current Deployment Status](#current-deployment-status)
* [Design](#design)
    * [Back Office Initial Designs](#back-office-initial-designs)
    * [Reporting](#reporting)
    * [Activity Stream](#activity-stream)
    * [Space Specific Analytics Screen](#space-specific-analytics-screen)
    * [Registration and Login](#registration-and-login)
    * [Activity Diagrams](#activity-diagrams)
    * [Sequence Diagrams](#sequence-diagrams)
* [System Manual](#system-manual)
    * [1. System Overview](#1-system-overview)
    * [2. System Architecture](#2-system-architecture)
    * [3. Technology Stack](#3-technology-stack)
    * [4. System Requirements](#4-system-requirements)
    * [5. Installation & Setup](#5-installation--setup)
    * [6. System Configuration](#6-system-configuration)
    * [7. Application Features](#7-application-features)
    * [8. API Documentation](#8-api-documentation)
    * [9. Database Schema](#9-database-schema)
    * [10. User Roles & Permissions](#10-user-roles--permissions)
    * [11. Deployment Guide](#11-deployment-guide)
    * [12. Monitoring & Analytics](#12-monitoring--analytics)
    * [13. Maintenance & Updates](#13-maintenance--updates)
* [User Manual](#user-manual)
    * [Login & Register & Logout](#login--register--logout)
    * [Node, Edge, Property Management](#node-edge-property-createupdatedelete)
    * [Space Functionalities](#space-functionalities)
    * [Reporting (User Type 1 - Regular User)](#reporting-user-type-1---regular-user)
    * [Managing Reports (Admins & Moderators)](#managing-reports-admins--moderators)
    * [Voting](#voting)
    * [Discussion](#discussion)
    * [Activity Stream](#activity-stream)
    * [Archive](#archive)
    * [Map](#map)
* [Mobile User Manual](#mobile-user-manual)
    * [Profile](#profile)
    * [Authentication](#authentication)
    * [Space Management](#space-management)
    * [Node Management](#node-management)
    * [Edge Management](#edge-management)
    * [Reporting](#reporting-1)
    * [Searching](#searching)
    * [Activity Stream](#activity-stream-1)
* [Test Results](#test-results)
    * [Automated Unit Tests](#automated-unit-tests)
    * [Unit Test Coverage Report](#unit-test-coverage-report)
    * [Manual Tests (Web)](#manual-tests)
    * [Mobile Manual Tests](#mobile-manual-tests)
* [Individual Contributions](#individual-contributions)
    * [Esra Nur Özüm](#esra-nur-özüm)
    * [Yasemin Tangül](#yasemin-tangül)
    * [Batuhan Cömert](#batuhan-cömert)
    * [Mehmet Yusuf Bayam](#mehmet-yusuf-bayam)
    * [Gamze Güneri](#gamze-güneri)
---

# Honor Codes

<img width="715" height="357" alt="Ekran görüntüsü 2025-12-20 233524" src="https://github.com/user-attachments/assets/b66ad806-2d04-45fb-a12d-66b22bc8b8a6" />

![WhatsApp Image 2025-12-20 at 23 46 03](https://github.com/user-attachments/assets/ceee7320-86bf-409b-a4f1-c431169622da)

<img width="527" height="270" alt="Screenshot from 2025-12-20 23-46-28" src="https://github.com/user-attachments/assets/f313e45e-13e0-4df3-868e-6355b5bee146" />



---

# List Of Deliverables

### 1) Repository / Release (GitHub)
- GitHub Release (tag: `v0.9`)  
  - Release message: “Swe574 2025 Fall - Final Project Report - Connect the Dots”
  - Release notes will be filled out
  - Release will be taken from **main branch**
- The final report and relevant final outputs will be shared in the repo under the `final-deliverables/` folder

### 2) Moodle Deliveries
- Final Project Group Report (to be uploaded to Moodle by the group communicator)
- Team Evaluation file (each team member will submit individually)

### 3) Hard Copy
- A bound printed final report will be prepared and delivered face to face.

### 4) Video
- Video of maximum 5 minutes for both web and mobile
- Video links will be given in the report

### 5) Deployment & Demo Information
- Deployment URL
- Deployment status (is it live, access notes, etc.)
- Dockerization status (Is there Docker, how to run it, etc.)
- Demo user information (role/account information required for testing)

### 6) Documentation to be Presented in the Report

-Table of Contents
- List of deliverables (this section)
- References / citations (if any)
- Project overview (planned vs delivered)
- SRS (Software Requirements Specification)
- Design (UML diagrams + mockup images)
- Requirements status 
- System manual (running requirements + Docker installation/running steps)
- User manual
- Test results (unit test coverage output + manual user tests)
---

# Application Videos
* Mobile : https://drive.google.com/file/d/1-k_mb2vfE9-6XmzvRf_2ZbHI4KMCLfN4/view?usp=sharing
* Web : https://drive.google.com/file/d/1aEl2GGNB25lBteWDdnxfFT2CV74BNWQS/view

---

# Demo User Information

Production ip address : http://13.60.88.202:3000/

Access Information(this account already created, you can login with username and password):

regular user:

username: freya

email : [freya@email.com](mailto:freya@email.com)

passowrd: freya123

profession : gardener

date of birth : 01/01/1995

country : Italy , Lusiana

admin access
username : admin , password: admin

Mobile (.apk goodle drive) link: https://drive.google.com/file/d/1RedqUrqyOo56tmUfhoqPUXQXXl9jkxuy/view?usp=sharing

---

# Project Overview (Executive Summary)
## Target according to SRS
- Users work together in Spaces to create node-edge knowledge graphs and enrich content/property with Wikidata. Managed by Admin / Space-based Moderator / User roles. It was a web + Android platform that offered reporting + archiving/restoring, activity streams, map exploration, analytics panels, and AI and Space summarization.

## What is actually delivered
- Backend (Django): Registration/login (JWT), profile fields (profession + location), Space create/join/leave, discussion + upvote/downvote, report system (case management + reason code), archive/restore (soft delete), ActivityStreams compatible activity feed, Wikidata property extraction/processing.
Graph & search: Adding node/edge in Space (via Wikidata), node/edge properties, type/grouping infrastructure. Advanced graph-search based on Neo4j (node/edge/property filters + depth).
- Web (React): Graph visualization, full-screen, instance-type filtering in SpaceDetails. Showing Spaces on the map with MapView (geocoding + grouping nearby locations). BackOffice screens (Users/Reports/Archive/Analytics), also Space analytics screen (in-space metrics/leaderboard approach).
- AI summarization: Compatible with SRS, markdown summary + node/edge/collaborator/discussion numbers returning endpoint and UI triggering with Gemini.
- Android (Kotlin/Compose): Login/Register, Space list/detail, node list/detail, adding node (Wikidata search + property selection), edge details, activity stream and profile screens.

## Missing/partial items according to SRS:
- Space merge (merge suggestion + thread merge) and STT/TTS were not implemented.
- “Similarity search” (embedding/cosine etc.) is not applied, searches are mostly text/ID/filter based.
- On the non-functional requirements side, requirements such as PII encryption-at-rest, HTTPS enforcement, font size customization are not clearly delivered.
- Overall result: The project captured SRS's "core product" vision on the web in a powerful way (graph + Wikidata + moderation + map + AI summary). 
- Android, on the other hand, covers fundamental usage flows. The biggest gaps are in merge/similarity-search and some non-functional requirements items.
---

# Software Requirements Specifications

### **Purpose**

The purpose of this document is to present a detailed description of the “Connect The Dots” project. This document will explain the purpose, scope, boundaries, and features of the system. It contains the answers to questions such as what the system will do, how the system should react to external stimuli, and under which constraints the system should operate. This document is intended for both the stakeholders and developers of the system.

### **Scope of The Project**

The aim of this project is to provide an environment for users to study, research, gain, and share knowledge in a collaborative manner. Users will be able to create, contribute, and have discussions on any topic they are interested in. This project will provide data visualization of related discussion items in a connected manner.

### **Glossary**

- **Regular User**: A standard user who can create and participate in collaboration spaces.
- **Moderator User**: A privileged user who can modify or remove inappropriate content within a specific, assigned space.
- **Admin User**: A privileged user with the ability to manage spaces, discussions, and user-generated content across the entire system.
- **Space**: A collaborative environment where users can add nodes, create discussions, and interact.
- **Graph Visualization**: A visual representation of nodes and edges within a collaboration space.
- **Discussion**: A threaded conversation within a space where users can post, reply, and vote.
- **Node**: A graphical element in a space representing a concept, idea, or piece of information.
- **Edge**: A connection between two nodes, representing relationships in the graphical visualization.
- **Tag**: Keywords assigned to spaces for categorization and searchability.
- **Search**: A functionality allowing users to find spaces by title/tags and other users by username.
- **Report**: A functionality allowing users to flag inappropriate content for moderators to check.
- **Analytics**: A functionality collects and visualizes application data to provide insights for admins and moderators.
- **Wikidata**: An external structured knowledge base integrated as a data source for the system.

---

### **1. Functional Requirements**

### **1.1. Registration And Login**

- 1.1.1. Users shall be able to register to the system with a unique e-mail, unique username, and password.
- 1.1.2. As part of the registration, users shall be required to provide their profession/work title and their current location.
- 1.1.3. The system shall capture location data in a structured GeoLocation format (e.g., latitude, longitude, or standardized place name).
- 1.1.4. The system shall require users to confirm they are over 18 years old via a mandatory checkbox.
- 1.1.5. Users shall be able to log in using their e-mail and password.

### **1.2. Authorization**

- 1.2.1. There shall be three types of user roles in the system: System Admin, Moderator, and Regular User.
- 1.2.2. A Regular User is a standard user who can create spaces, join spaces, and contribute content.
- 1.2.3. A Moderator is a role assigned on a per-space basis. A user can be a Moderator in one space and a Regular User in another.
- 1.2.4. A Moderator shall have all the privileges of a Regular User within their assigned space, plus the ability to archive nodes, discussions, and comments within that space.
- 1.2.5. A System Admin shall have universal privileges across the entire system, including all abilities of a Regular User and a Moderator in any space.
- 1.2.6. The creator of a space will be automatically assigned as Moderator by the system and shall be able to assign or revoke the Moderator role to any other collaborator within that space.
- 1.2.7. When a Moderator archives content, the system shall require them to provide a brief justification for the action.
- 1.2.8. A System Admin shall be able to archive any content (spaces, nodes, comments) or remove users from the system. This action will function as a "soft delete," making the content invisible to users but restorable from the back-office.
- 1.2.9. A System Admin shall be able to restore previously archived content, making it visible within the system again.
- 1.2.10. A System Admin shall be able to manage any user's role (e.g., promote a user to be another System Admin) from the back-office.
- 1.2.11. The system shall provide a back-office dashboard for Admins to manage users, roles, and review archived or reported content.
- 1.2.11. The system shall allow administrators to view comprehensive application analytics data, including user activity, content trends, and performance metrics, while moderators shall only be able to view analytics specific to the spaces they moderate.


### **1.3. Collaboration Spaces**

- 1.3.1. A user shall be able to create spaces by providing a title, description, and tags.
- 1.3.2. A user shall be able to join existing spaces as a collaborator.
- 1.3.3. Within the discussion area of a space, a collaborator shall be able to propose merging their space with another similar space.
- 1.3.4. When two spaces are merged, the discussion threads from both spaces shall be combined and displayed in a single, chronologically sorted thread.
- 1.3.5. A user shall be able to reply to discussions in a joined space.
- 1.3.6. A space shall have a title, graphical visualization, and discussion area.
- 1.3.7. A space shall have tags to make it easier for users to categorize, search, and find spaces.
- 1.3.8. The system shall store discussions with version history.
- 1.3.9. The system shall display an In-Space Activity Stream within each space, listing key events relevant to that space in reverse chronological order.
- 1.3.10. The In-Space Activity Stream shall log, at a minimum, the following event types:
    - A new collaborator joining the space.
    - A new node being added.
    - A new edge (connection) being created.
    - A new discussion comment being posted.
    - A space being formed from a merge, naming the source spaces.
- 1.3.11. The system shall display the number of votes on space content.
- 1.3.12. The user shall be able to report spaces, nodes, and user comments for inappropriate content.
- 1.3.13. The system shall provide an informational onboarding interface within space details that explains key features and functionality of the space, including graph visualization, search capabilities, activity streams, and location features.
- 1.3.14. The system shall display a map visualization within space details showing the geographical locations associated with nodes in the space, when location data is available.
- 1.3.15. The system shall provide a map view for browsing and discovering spaces, displaying spaces as markers on an interactive map based on their location data.

### **1.4. Graph Presentation**

- 1.4.1. The system shall lay out the graphical visualization of a space which shows the relation between contents of the space by nodes and edges.
- 1.4.2. Users shall be able to add new nodes by connecting them to existing nodes in a space.
- 1.4.3. The system shall show a bi-directional connection between two nodes as an edge.
- 1.4.4. The system shall show the relation property of an edge on the graph representation.
- 1.4.5. A user shall be able to add, delete, or modify nodes in a joined space.
- 1.4.6. A user shall be able to add or remove a connection between nodes in a joined space.
- 1.4.7. A user shall be able to create and edit properties associated with nodes within a joined space.
- 1.4.8. The system shall allow users to select and modify properties from available Wikidata properties when creating or editing nodes.
- 1.4.9. The system shall group node properties by their property ids for improved organization and display.
- 1.4.10. The system shall visually distinguish nodes by applying color coding based on their instance type groups, making it easier to identify different categories of entities in the graph.
- 1.4.11. The system shall provide filtering functionality that allows users to filter nodes by their instance type, enabling focused exploration of specific entity categories within the graph.
- 1.4.12. The system shall provide a full-screen mode for the space graph visualization, allowing users to view and interact with the graph in an expanded view for better visibility and navigation.

### **1.5. In-Space Search**

- 1.5.1. A user shall be able to search in spaces by title, description, and tags.
- 1.5.2. The system shall apply a similarity search among nodes and edges of that space according to user input.
- 1.5.3. A user shall be able to navigate to a node or edge result using the in-space search result.
- 1.5.4. The system shall provide advanced search functionality within spaces that allows users to construct complex queries using node properties, edge properties and specifying relation level between queries.
- 1.5.5. The system shall display advanced search results in a graph layout which highlights the path between results and a list of results including nodes, edges and properties.

### **1.6. In-App Search**

- 1.6.1. A user shall be able to search for spaces, nodes, edges, and profiles by text or keyword.
- 1.6.2. The system shall find related content using a similarity search.
- 1.6.3. The system shall display search results according to the matching type of content (space, node, etc.).
- 1.6.4. A user shall be able to navigate to a profile from search results.
- 1.6.5. A user shall be able to navigate to a space from search results.

### **1.7. Browsing**

- 1.7.1. A user shall be able to browse new spaces.
- 1.7.2. A user shall be able to browse trending spaces.

### **1.8. User Profile**

- 1.8.1. The system shall not expose users’ sensitive information, such as their email address, on their public profile.
- 1.8.2. The system shall show users’ application-related information such as username, age, profession, and location.
- 1.8.3. The system shall list a user's created spaces, collaborations, and activities on their profile screen.
- 1.8.4. A user shall be able to view other users' profile pages.
- 1.8.5. A user shall be able to edit their own profile information.

### **1.9. Discussion**

- 1.9.1. The system shall display the discussion of a space in the format of an activity stream.
- 1.9.2. Users shall be able to add comments to a discussion in a joined space.
- 1.9.3. A user shall be able to vote up or down on discussions in a joined space.

### **1.10. Analytics**

- 1.10.1. The system shall provide a space-level analytics dashboard visible to the moderators.
- 1.10.2. The space-level dashboard shall display key metrics, including collaborator activity, number of new nodes and discussions over time, and content engagement (votes, replies).
- 1.10.3. The system shall provide a system-level analytics dashboard visible only to System Admins.
- 1.10.4. The system-level dashboard shall display platform-wide metrics, including new user registration rates, total number of active spaces, and overall user activity trends.

### **1.11. User Reputation**

- 1.11.1. The system shall identify and display a "Top Contributor" label on the profiles of users who demonstrate high levels of positive engagement within a space (e.g., frequent valuable contributions, receiving upvotes, participating in discussions).
- 1.11.2. A full user scoring and reputation system is planned for a future release and is not included in the initial version of the project.

### **1.12. Main Activity Stream**

- 1.12.1. The system shall display a Main Activity Stream on the application's home page.
- 1.12.2. This stream shall aggregate and display significant public events from across the entire platform to encourage discovery.
- 1.12.3. The Main Activity Stream shall show events such as:
    - The creation of a new public space.
    - A space that is rapidly gaining collaborators or content ("trending").
    - Highly-voted discussions or significant nodes from public spaces.
- 1.12.4. The stream shall not be cluttered with minor events (e.g., individual comments, minor node edits) unless they meet a significance threshold (like numerous upvotes).
- 1.12.5. Each item listed in the Main Activity Stream shall be a direct link that allows the user to navigate to the corresponding space or content.
- 1.12.6. The system shall support mentioning users in activity stream entries, allowing users to be referenced in activity summaries through clickable username links.
- 1.12.7. When a user is mentioned in an activity stream entry, the system shall make the username clickable, enabling navigation to the mentioned user's profile.

### **1.13. AI-Powered Space Summarization**

- 1.13.1. The system shall provide an AI-powered summarization feature that generates comprehensive summaries of collaboration spaces.
- 1.13.2. The AI summary shall analyze the space's graph structure, including nodes, edges, and their relationships, to provide insights about the knowledge graph.
- 1.13.3. The AI summary shall incorporate information about the space's discussions, collaborators, and activity to provide context about collaboration and engagement.
- 1.13.4. The system shall generate summaries that highlight key entities, relationships, patterns, and insights within the space.
- 1.13.5. The AI summary shall be formatted in a readable markdown format with organized sections for overview, key entities, relationships, insights, and activity metrics.
- 1.13.6. The system shall display metadata alongside the AI summary, including the number of nodes, edges, and collaborators in the space.

### **1.14. Map-Based Space Discovery**

- 1.14.1. The system shall provide a map view for browsing and discovering spaces based on their geographical locations.
- 1.14.2. The map view shall display spaces as markers on an interactive map, positioned according to their location data (city, country, or coordinates).
- 1.14.3. The system shall cluster nearby spaces on the map when multiple spaces are located in close proximity, improving map readability and performance.
- 1.14.4. Users shall be able to click on space markers or clusters to view space details and navigate to the full space view.
- 1.14.5. The map view shall support geocoding of spaces that have location information but lack precise coordinates, automatically converting location names to map coordinates.
- 1.14.6. The system shall display space information in map popups, including space title, description preview, and location details.

---

### **2. Non-functional Requirements**

### **2.1. Performance**

- 2.1.1. The system should have a responsive UI to support using the project on mobile devices.
- 2.1.2. Page load times for typical spaces shall be under 3 seconds.

### **2.2. Reliability**

- 2.2.1. The system shall ensure 99.9% uptime.

### **2.3. Security**

- 2.3.1. All user passwords shall be securely hashed using a modern algorithm (e.g., bcrypt).
- 2.3.2. All personally identifiable information (PII), such as user emails, shall be encrypted at rest.
- 2.3.3. The system shall use HTTPS for all client-server communication.

### **2.4. Accessibility**

- 2.4.1. The system shall have English as the main language.
- 2.4.2. The system shall have localization for different languages.
- 2.4.3. The system shall provide an activity stream in compliance with W3C standards.
- 2.4.4. The system shall present content in a format that is accessible and easily distinguishable for users with color vision deficiency.
- 2.4.5. The system shall allow users to customize font and content size to enhance readability and usability.
- 2.4.6. The system shall support speech-to-text input for core content creation, including adding new nodes, creating connections (edges) between nodes, and posting comments in discussions.
- 2.4.7. The system shall include audible feedback functionality that reads on-screen content aloud, enabling users with visual impairments to interact with the interface through audio output.

---

### **3. System Architecture**

### **3.1. Architecture**

- 3.1.1. The system shall be available as a web & Android application.
- 3.1.2. The system shall have a backend with Django Python or Spring Boot Java.
- 3.1.3. The system shall use a relational database, PostgreSQL, to store data such as users, spaces, and discussions.
- 3.1.4. The system shall have two different deployment environments: one for development & testing and one for production.
- 3.1.5. The system shall be deployed on an AWS EC2 Instance.
- 3.1.6. The system for the Android client shall be distributed as an APK through Firebase App Distribution.

### **3.2. Integration**

- 3.2.1. The system shall utilize Wikidata as a data source.
---

# Status of the Project


Functional Requirement | Description | Backend | Web | Mobile
-- | -- | -- | -- | --
1.1. Registration And Login |   |   |   |  
1.1.1 | Register with a unique e-mail, username, and password. | Done ✅ | Done ✅ | Done ✅
1.1.2 | Provide profession/work title and current location during registration. | Done ✅ | Done ✅ | Done ✅
1.1.3 | Capture location data in a structured GeoLocation format. | Done ✅ | Done ✅ | Done ✅
1.1.4 | Require users to confirm they are over 18 via a checkbox. | Done ✅ | Done ✅ | Done ✅
1.1.5 | Log in using e-mail and password. | Done ✅ | Done ✅ | Done ✅
1.2. Authorization |   |   |   |  
1.2.1 | Three user roles: System Admin, Moderator, and Regular User. | Done ✅ | N/A | N/A
1.2.2 | Regular Users can create, join, and contribute to spaces. | Done ✅ | Done ✅ | Done ✅
1.2.3 | The moderator role is assigned on a per-space basis. | Done ✅ | Done ✅ | Done ✅
1.2.4 | Moderators can archive content within their assigned space. | Done ✅ | Done ✅ | N/A
1.2.5 | System Admin has universal privileges across the system. | Done ✅ | Done ✅ | N/A
1.2.6 | The creator of a space is automatically assigned as Moderator. | Done ✅ | N/A | N/A
1.2.7 | Moderators must provide a justification when archiving content. | Not Done | Not Done | Not Done
1.2.8 | System Admin can "soft delete" content and users. | Done ✅ | Done ✅ | N/A
1.2.9 | System Admin can restore previously archived content. | Done ✅ | Done ✅ | N/A
1.2.10 | System Admin can manage any user's role from the back-office. | Done ✅ | Done  ✅ | N/A
1.2.11 | Back-office dashboard for Admins to manage the system. | Done ✅ | Done ✅ | N/A
1.2.11 | Admins view comprehensive analytics, moderators view space-specific data. | Done ✅ | Done ✅ | N/A
1.3. Collaboration Spaces |   |   |   |  
1.3.1 | Create spaces with a title, description, and tags. | Done ✅ | Done ✅ | Done ✅
1.3.2 | Join existing spaces as a collaborator. | Done ✅ | Done ✅ | Done ✅
1.3.3 | Propose merging their space with another. | Not Done | Not Done | Not Done
1.3.4 | When merged, discussion threads are combined. | Not Done | N/A | N/A
1.3.5 | Reply to discussions in a joined space. | Done ✅ | Done ✅ | Done ✅
1.3.6 | A space has a title, graphical visualization, and discussion area. | Done ✅ | Done ✅ | Done ✅
1.3.7 | A space has tags for categorization and search. | Done ✅ | Done ✅ | Done ✅
1.3.8 | Store discussions with version history. | Not Done | N/A | N/A
1.3.9 | In-Space Activity Stream lists key events within each space. | Done ✅ | Done ✅ | In Progress
1.3.10 | The stream logs new collaborators, nodes, edges, and discussions. | Done ✅ | Done ✅ | In Progress
1.3.11 | Display the number of votes on space content. | Done ✅ | Done ✅ | Done ✅
1.3.12 | Report spaces, nodes, and comments for inappropriate content. | Done ✅ | Done ✅ | Done ✅
1.3.13 | Onboarding info in space details | N/A | Done ✅ | N/A
1.3.14 | Map visualization in space detail | Done ✅ | Done ✅ | N/A
1.3.15 | Map visualization for spaces in home page | Done ✅ | Done ✅ | N/A
1.4. Graph Presentation |   |   |   |  
1.4.1 | Graphical visualization of nodes and edges in a space. | Done ✅ | Done ✅ | Done ✅
1.4.2 | Add new nodes by connecting them to existing nodes. | Done ✅ | Done ✅ | Done ✅
1.4.3 | Show a bi-directional connection between two nodes as an edge. | Done ✅ | Done ✅ | Done ✅
1.4.4 | Show the relation property of an edge on the graph. | Done ✅ | Done ✅ | Done ✅
1.4.5 | Add, delete, or modify nodes in a joined space. | Done ✅ | Done ✅ | Done ✅
1.4.6 | Add or remove a connection between nodes. | Done ✅ | Done ✅ | Done ✅
1.4.7 | Add or edit Wikidata properties during Node creation | Done ✅ | Done ✅ | Done ✅
1.4.8 | System shall allow modification of properties | Done ✅ | Done ✅ | Done ✅
1.4.9 | Group properties with same id in node details & node creation | Done ✅ | Done ✅ | Done ✅
1.4.10 | Color distinguish nodes by instance type | Done ✅ | Done ✅ | N/A
1.4.11 | Filter nodes & highlight in space graph by instance type | Done ✅ | Done ✅ | N/A
1.4.12 | Space graph should support fullscreen mode | N/A | Done ✅ | N/A
1.5. In-Space Search |   |   |   |  
1.5.1 | Search in spaces by node title, node property, node property value and edge based on the depth | Done  ✅ | Done  ✅ | Not Done
1.5.2 | Apply a similarity search among nodes and edges. | Not Done | N/A | N/A
1.5.3 | Navigate to a node or edge from the in-space search result. | Done  ✅ | Done  ✅ | Not Done
1.5.4 | System shall support advanced search with supporting queries with nodes, edges, properties and relation level | Done  ✅ | Done  ✅ | N/A
1.5.5 | System shall support graph & list display for search results | Done  ✅ | Done  ✅ | N/A
1.6. In-App Search |   |   |   |  
1.6.1 | Search for spaces, nodes, edges, and profiles by text. | In Progress | Done ✅ | Done ✅
1.6.2 | Find related content using a similarity search. | Not Done | N/A | N/A
1.6.3 | Display search results according to the content type. | Done ✅ | Done ✅ | Done ✅
1.6.4 | Navigate to a profile from search results. | N/A | Done ✅ | Done ✅
1.6.5 | Navigate to a space from search results. | N/A | Done ✅ | Done ✅
1.7. Browsing |   |   |   |  
1.7.1 | Browse new spaces. | Done ✅ | Done ✅ | Done ✅
1.7.2 | Browse trending spaces. | Done ✅ | Done ✅ | Done ✅
1.8. User Profile |   |   |   |  
1.8.1 | Do not expose users’ sensitive information on their public profile. | Done ✅ | Done ✅ | Done ✅
1.8.2 | Show username, age, profession, and location on profiles. | Done ✅ | Done ✅ | Done ✅
1.8.3 | List a user's created spaces, collaborations, and activities. | Done ✅ | Done ✅ | Done ✅
1.8.4 | View other users' profile pages. | Done ✅ | Done ✅ | Done ✅
1.8.5 | Edit your own profile information. | Done ✅ | Done ✅ | Done ✅
1.9. Discussion |   |   |   |  
1.9.1 | Display the discussion of a space as an activity stream. | Done ✅ | Done ✅ | Done ✅
1.9.2 | Add comments to a discussion in a joined space. | Done ✅ | Done ✅ | Done ✅
1.9.3 | Vote up or down on discussions. | Done ✅ | Done ✅ | Done ✅
1.10. Analytics |   |   |   |  
1.10.1 | The space-level analytics dashboard is visible to moderators. | Done ✅ | Done ✅ | N/A
1.10.2 | The dashboard displays metrics on activity, content, and engagement. | Done ✅ | Done ✅ | N/A
1.10.3 | System-level analytics dashboard is visible only to System Admins. | Done ✅ | Done ✅ | N/A
1.10.4 | The dashboard displays platform-wide metrics on users and activity. | Done ✅ | Done ✅ | N/A
1.11. User Reputation |   |   |   |  
1.11.1 | Display a "Top Contributor" label on certain user profiles. | Display a "Top Contributor" label on certain user profiles. | Partially Done in Space Analytics | Not Done
1.11.2 | A full user scoring and reputation system in space details. | Done ✅ | Done ✅ | Not Done
1.12. Main Activity Stream |   |   |   |  
1.12.1 | Display a Main Activity Stream on the home page. | Done ✅ | Done ✅ | Done ✅
1.12.2 | The stream aggregates significant public events. | Done ✅ | N/A | N/A
1.12.3 | The stream shows new spaces, trending spaces, and highly-voted content. | Not Done | Not Done | In Progress
1.12.4 | The stream is not cluttered with minor events. | In Progress | N/A | N/A
1.12.5 | Each item in the stream is a direct link to the content. | Done ✅ | Done ✅ | Not Done
1.12.6 | Support mentioning users in activity stream items | Done ✅ | Done ✅ | Not Done
1.12.7 | System shall support navigation to user profile from activity stream item | Done ✅ | Done ✅ | Not Done
1.13. AI Summary of Space |   |   |   |  
1.13.1 | System shall support AI summary feature | Done ✅ | Done ✅ | N/A
1.13.2 | AI summary shall analyze graph structure, nodes, edges and their relations | Done ✅ | Done ✅ | Not Done
1.13.3 | AI summary shall analyze discussions, collaborators, analytics of space | Done ✅ | Done ✅ | Not Done
1.13.4 | System shall generate summaries that highlight key nodes, relations and insights | Done ✅ | Done ✅ | Not Done
1.13.5 | System shall support summary in Markdown format with sections | Done ✅ | Done ✅ | Not Done
1.13.6 | System shall include metadata metrics and statistics alongside summary | Done ✅ | Done ✅ | Not Done
1.14. Map Based Discovery |   |   |   |  
1.14.1 | The system shall provide a map view for browsing and discovering spaces | Done ✅ | Done ✅ | N/A
1.14.2 | The map view shall display spaces as markers on an interactive map | Done ✅ | Done ✅ | N/A
1.14.3 | The system shall cluster nearby spaces on the map when multiple spaces are located in close proximity | N/A | Done ✅ | N/A
1.14.4 | Users shall be able to click on space markers or clusters to view space details and navigate to the full space view | N/A | Done ✅ | N/A
1.14.5 | The map view shall support geocoding of spaces that have location information but lack precise coordinates, automatically converting location names to map coordinates. | Done ✅ | Done ✅ | N/A
1.14.6 | The system shall display space information in map popups, including space title, description preview, and location details. | Done ✅ | Done ✅ | N/A


---
# Dockerization Status

The Connect-The-Dots project has comprehensive containerization using Docker and Docker Compose. All application components are fully containerized with a production-ready architecture. The Dockerization implementation uses DevOps practices including multi-stage builds, health monitoring, persistent storage, and multi-environment configuration support. 

## Container Arhitecture Overview

1. Backend Service (Django API)
    - Base Image: python:3.13.3-slim
    - Application Server: Gunicorn WSGI server (production-grade)
    - Exposed Port: 8000
    - Key Features:
    PostgreSQL client libraries installed (libpq-dev, psycopg2-binary)
    APT cache cleanup implemented to minimize image size
    Automated database migrations on startup
    Background task execution

1. Frontend Service (React Application)
    - Build Stage: node:22-alpine
    - Runtime Stage: nginx:1.28-alpine
    - Exposed Port: 80
    - Architecture: Multi-stage Docker build
    - Key Features:
    Two-stage build process reducing final image size by approximately 90%
    Dynamic API URL configuration via build arguments
    Production-optimized static file serving through Nginx
    SPA routing support with fallback to index.html
    Legacy peer dependency handling

1. Database Services
    - PostgreSQL: postgres:16-alpine for relational data storage
    - Neo4j: neo4j:5.15.0 for graph database functionality
    - Grafana: grafana/grafana:latest for analytics and monitoring

## Multi-Environment Configuration

1. Health Check
    
    All critical services implement Docker health checks ensuring service availability before dependent services start:
    
    - PostgreSQL Health Check
    - Frontend Health Check
    - Grafana Health Check

1. Data Persistence
Named volumes ensure data persistence across container restarts:
    - postgres_data - Database records
    - grafana_data - Analytics dashboards and metrics
    - grafana_config - Grafana configuration
    - neo4j_data - Graph database storage

1. Service Orchestration
Dependency management implemented through depends_on:
    - Frontend depends on API availability
    - API depends on Database readiness
    - Grafana depends on Database connection


---
# Status of the Deployment

Local: `http://0.0.0.0:3000/`

Test Server: `16.171.44.79:3000`

Test Server Grafana: `16.171.44.79:3001`

Production Server: `13.60.88.202:3000`

Production Server Grafana: `13.60.88.202:3001`

Neo4j Server: `13.60.235.0:7474` -> Connected to the Production Server PostgreSQL

## Deployment Architecture

Connect the dots project designed as microservice architecture using Docker with the following components:

Core Services:

- Backend API: Django REST API with Gunicorn (Python 3.13.3)
- Frontend: React application served via Nginx (Node 22)
- Database: PostgreSQL 16 Alpine
- Graph Database: Neo4j 5.15.0 (for graph visualization)
- Analytics: Grafana (latest) for monitoring and analytics dashboards

## Deployment Environments

This project contains 3 different deployment configurations based on the deployment purpose

1. Development Environment (Local Usage)
- Local development setup
- Includes all services including Neo4j
- API accessible at localhost:8000
- Frontend at localhost:3000
- Grafana at localhost:3001
- Neo4j at localhost:7474

1. Staging/Development Server 
- Deployed to AWS EC2 (IP: 16.171.44.79:3000)
- Automated deployment triggered on push to develop branch
- Same port configuration as dev environment

1. Production Server
- Deployed to AWS EC2 (IP:13.60.88.202:3000)
- Automated deployment triggered on push to main branch
- Uses Neo4j from AWS EC2 (IP:13.60.235.0:7474)

## Automated Deployment

The project has an **automated deployment pipeline** using GitHub Actions:


<img width="935" height="771" alt="Screenshot from 2025-12-20 12-27-51" src="https://github.com/user-attachments/assets/b2396981-df41-4d9c-9a1e-4ca3f4050576" />

<img width="921" height="810" alt="Screenshot from 2025-12-20 12-29-05" src="https://github.com/user-attachments/assets/a689c2da-4747-45cf-b780-a021f4645220" />


### Web Application CI/CD

- Feature Branches:
    - Automated testing on pull requests
    - Test results published to GitHub PR comments
    - Automated unit testing
- Develop Branch:
    - Runs full test suite (backend unit tests, linting, health checks)
    - Auto-deploys to staging EC2 on successful push
    - Security group management with IP whitelisting
    - Test results published to GitHub PR comments
    - Automated unit testing
- Main Branch:
    - Runs full test suite
    - Auto-deploys to production EC2 on successful push
    - Separate production environment
    - Security group management with IP whitelisting
    - Test results published to GitHub PR comments
    - Automated unit testing

### Mobile Application CI/CD

- Android APK builds using Gradle
- Automated unit testing
- APK artifacts generated with naming convention: {issue-number}-{commit-SHA}-beta.apk
- Separate workflows for feature, develop, and main branches
- Test results published to GitHub PR comments

## Deployment Process

### **Automated Deployment Steps:**

- GitHub Actions whitelists runner IP in AWS security group
- SSH into EC2 instance
- Clone/pull latest code from respective branch
- Copy environment variables from secure location
- Execute docker compose up -d --build with appropriate compose file
- Remove runner IP from security group

### Manual Deployment:

Simple bash script `deploy.sh` available for manual deployments
Pulls latest code, rebuilds containers, and runs migrations

## Current Deployment Status:

### Fully Operational:

- Automated CI/CD pipeline for both web and mobile
- Multi-environment deployment (dev/staging/production)
- Containerized architecture with health checks
- Monitoring and analytics infrastructure
- Database migration automation
- Automated unit testing & result reporting & code coverage checks


---
# Design

## Back Office Initial Designs

<img width="934" height="580" alt="Screenshot 2025-10-19 at 18 56 03" src="https://github.com/user-attachments/assets/690b7a20-34ea-4a9f-ae7a-1cfc5b9502cc" />
<img width="934" height="578" alt="Screenshot 2025-10-19 at 18 56 30" src="https://github.com/user-attachments/assets/483933fe-8dab-4d2e-8a2c-9cb6f808040b" />
<img width="936" height="580" alt="Screenshot 2025-10-19 at 18 56 42" src="https://github.com/user-attachments/assets/e8ba918d-1b08-412d-8ca7-5715a8ba8f34" />
<img width="930" height="576" alt="Screenshot 2025-10-19 at 18 57 08" src="https://github.com/user-attachments/assets/736e8b26-5499-408d-802b-f76dbf13ce2c" />


## Reporting

Reporting a space and a discussion comment
<img alt="Screenshot 2025-10-19 at 19 19 35" src="https://github.com/user-attachments/assets/78229ba2-a4b7-46ae-b843-2242abe9eb1c" />

Reporting a Node
<img alt="Screenshot 2025-10-19 at 19 32 36" src="https://github.com/user-attachments/assets/65040b33-77f9-4e69-94f7-ba2bfe69162f" />

Activity Stream

<img alt="Screenshot 2025-10-19 at 20 13 49" src="https://github.com/user-attachments/assets/e01a25a6-0e5c-424d-bc7a-c53132d35433" />


Space Specific Analytics Screen

<img alt="Screenshot 2025-10-27 at 19 42 30" src="https://github.com/user-attachments/assets/b1b621f9-dffb-4567-b33b-c750550b0769" />

### Registration and Login
<img width="184" height="370" alt="image" src="https://github.com/user-attachments/assets/ec058cd1-7621-4a8b-8772-267049b4f0fb" />                      <img width="182" height="372" alt="image" src="https://github.com/user-attachments/assets/8e1f8375-50d7-422d-8dbc-d7d639308cf4" />

### Profile Page
<img width="172" height="600" alt="image" src="https://github.com/user-attachments/assets/0ceb111d-55ed-4801-9481-d91a58fe2ae4" />
<img width="240" height="515" alt="image" src="https://github.com/user-attachments/assets/d37fb810-f720-408e-94db-4348d72c5a2e" />
<img width="240" height="515" alt="image" src="https://github.com/user-attachments/assets/31f7fcb8-9843-4082-b961-74fd015ffd37" />

### Edit Profile Page
<img width="599" height="514" alt="image" src="https://github.com/user-attachments/assets/9b3ce3eb-bf17-449c-b183-7c20437a574b" />

### Feed page
<img width="256" height="565" alt="image" src="https://github.com/user-attachments/assets/10c969dd-56dd-4d0b-ab37-2ab18f764648" />

### Space Details Page
<img width="225" height="738" alt="image" src="https://github.com/user-attachments/assets/80e0fdcd-a61a-40b7-bc63-ed9c67f78a11" />
<img width="640" height="562" alt="image" src="https://github.com/user-attachments/assets/92915a67-8602-433f-b7e0-ef70088d56db" />
<img width="1142" height="730" alt="image" src="https://github.com/user-attachments/assets/7a45ee12-4453-4003-a4df-26b7410ea8f9" />
<img width="1071" height="642" alt="image" src="https://github.com/user-attachments/assets/5881cd85-87b8-4c18-b5ca-823d3e43238f" />

## Activity Diagrams

### User Logout

<img width="604" height="421" alt="logout-activity-diagram" src="https://github.com/user-attachments/assets/7921b991-5d7d-4c5a-9cee-5505cc3ceb91" />

## Edit Profile
<img width="514" height="417" alt="edit-profile-web-activity-diagram" src="https://github.com/user-attachments/assets/3da9c14e-6b30-46fe-8cd6-6e5864c80efb" />
<img width="562" height="417" alt="edit-profile-mobile-activity-diagram" src="https://github.com/user-attachments/assets/d5eea30b-2477-48bc-91c0-6f6024e24298" />

## Report Space, User profile, and Discussion Comment
<img width="679" height="826" alt="activity diagrams" src="https://github.com/user-attachments/assets/25d03978-4bc6-4e70-83cd-977c49767ade" />

## Add Discussion Comment
<img width="274" height="532" alt="add-comment-activity-diagram" src="https://github.com/user-attachments/assets/40eea929-91b6-48f5-b441-79f2f56f84ce" />

## Add Node & Property
<img width="475" height="1446" alt="CTD-ActivityDiagram-AddNodeAndProperty" src="https://github.com/user-attachments/assets/dc484cf2-fa09-4681-ae6d-d99887bece44" />

## Archive Content
<img width="1666" height="1069" alt="CTD-ActivityDiagram-Archive" src="https://github.com/user-attachments/assets/492d6507-bec2-48a6-bc68-54e737bedff1" />

## Restore Content
<img width="1536" height="1535" alt="CTD-ActivityDiagram-Restore" src="https://github.com/user-attachments/assets/35ad1020-111e-487e-a413-6e8c2dff6c48" />

## Sequence Diagrams

### Register User
<img width="1036" height="649" alt="SequenceDiagram_Register" src="https://github.com/user-attachments/assets/1a2587bd-554e-44e0-95a4-77fb0f7e15e0" />

### Login User
<img width="871" height="559" alt="SequenceDiagram_Login" src="https://github.com/user-attachments/assets/e449efec-6e40-47d2-b535-05a2aa9817ac" />

### Create Space
<img width="658" height="707" alt="create-space-sequence drawio" src="https://github.com/user-attachments/assets/60342bc8-25fb-4920-a453-a45da976ca6d" />

### Add Node to Space
<img width="1433" height="962" alt="SequenceDiagram_AddNode" src="https://github.com/user-attachments/assets/44809931-84d5-4fde-b911-e89ca9b59709" />


### Report functionality
<img width="1681" height="2180" alt="ConnectTheDots-Report-SequenceDiagram" src="https://github.com/user-attachments/assets/74f64469-c4b4-4674-a635-d9643290ca85" />

### AI summary feature
<img width="1204" height="1201" alt="resim" src="https://github.com/user-attachments/assets/2a8d9a7c-eecf-419d-8045-01825a72eae7" />



---

# System Manual

## 1. System Overview

### 1.1 Purpose

System Manual is prepared to describe the system for both web an mobile, including, system requirements, system architecture, database structure, application configurations and much more deep dive information to system itself.

## 2 System Architecture

### 2.1 High Level System Architecture

<img width="477" height="523" alt="Screenshot from 2025-12-20 12-48-10" src="https://github.com/user-attachments/assets/2174032e-112c-4a11-a15b-df1e147aa53f" />

### 2.2 Component Breakdown
### 2.2.1 Frontend Layer

* Web Application: React 19.0.0 with modern hooks
* Mobile Application: Kotlin with Jetpack Compose
* State Management: Context API (Web), StateFlow (Mobile)
* Routing: React Router v7 (Web), Jetpack Navigation (Mobile)

### 2.2.2 Backend Layer
* Framework: Django 5.1.7 with Django REST Framework 3.15.2
* WSGI Server: Gunicorn 23.0.0
* Authentication: JWT (djangorestframework_simplejwt 5.5.0)
* API Documentation: drf-spectacular 0.27.2

### 2.2.3 Data Layer
* Relational Database: PostgreSQL 16-alpine
* Graph Database: Neo4j 5.15.0
* Analytics: Grafana (latest)

## 3. Technology Stack
### 3.1 Web Frontend

| Component              | Technology          | Version |
|------------------------|---------------------|---------|
| Framework              | React               | 19.0.0  |
| Build Tool             | Vite                | 6.2.0   |
| HTTP Client            | Axios               | 1.8.2   |
| Routing                | React Router DOM    | 7.3.0   |
| Graph Visualization    | ReactFlow           | 11.11.4 |
| Map Integration        | React Leaflet       | 4.2.1   |
| Internationalization   | i18next             | 25.6.0  |
| Testing                | Vitest              | 3.0.9   |

### 3.2 Mobile Application

| Component              | Technology                     | Version |
|------------------------|--------------------------------|---------|
| Language               | Kotlin                         | 1.9.0+  |
| UI Framework           | Jetpack Compose                | Latest  |
| Dependency Injection   | Hilt                           | Latest  |
| Architecture           | Clean Architecture + MVVM      | -       |
| HTTP Client            | Retrofit                       | Latest  |
| Min SDK                | 26 (Android 8.0)               | -       |
| Target SDK             | 36                             | -       |

### 3.3 Backend

| Component           | Technology              | Version |
|---------------------|-------------------------|---------|
| Language            | Python                  | 3.13.3  |
| Framework           | Django                  | 5.1.7   |
| REST Framework      | Django REST Framework   | 3.15.2  |
| Database Driver     | psycopg2-binary         | 2.9.10  |
| Neo4j Driver        | neo4j                   | 5.15.0+ |
| AI Integration      | google-generativeai     | 0.8.3   |
| WSGI Server         | Gunicorn                | 23.0.0  |


### 3.4 Infrastructure
| Component          | Technology       | Version |
|--------------------|------------------|---------|
| Containerization   | Docker           | Latest  |
| Orchestration      | Docker Compose   | 3.3     |
| Reverse Proxy      | Nginx            | 1.28-alpine |
| Database           | PostgreSQL       | 16-alpine  |
| Graph Database     | Neo4j            | 5.15.0  |
| Analytics          | Grafana          | Latest  |
| Cloud Provider     | AWS EC2          | -       |


## 4. System Requirements

### 4.1 Development Environment

Minimum Requirements:

* CPU: 4 cores
* RAM: 8 GB
* Storage: 20 GB free space
* OS: Linux, macOS, or Windows 10+

Software Prerequisites:

* Docker 20.10+
* Docker Compose 1.29+
* Git 2.30+
* Node.js 22+ (for frontend development)
* Python 3.13+ (for backend development)
* Android Studio (for mobile development)

### 4.2 Production Environment

Server Specifications:

* CPU: 2+ cores
* RAM: 4 GB minimum, 8 GB recommended
* Storage: 50 GB+ with SSD
* Network: Stable internet connection

Software Requirements:

* Docker Engine 20.10+
* Docker Compose 1.29+
* Ubuntu 20.04+ or compatible Linux distribution

### 4.3 Client Requirements

Web Browser:

* Chrome 90+
* Firefox 88+
* Safari 14+
* Edge 90+
* Mobile:

Mobile: 

* Android 8.0 (API 26) or higher
* 100 MB free storage
* Internet connection

## 5. Installation & Setup
### 5.1 Local Development Setup (Web)

Step 1: Clone Repository
```
git clone https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots.git
cd SWE574-ConnectTheDots
```

Step 2:  Backend Configuration
```
cd backend
cp .env.docker.template .env

# Edit .env.docker with your credentials
nano .env
# or
vim .env
```
Copy and paste the following configurations to .env file
```
DJANGO_SECRET_KEY=your-secret-key-here
DJANGO_DEBUG=True
POSTGRES_USER=myuser
POSTGRES_PASSWORD=securepassword
POSTGRES_DB=mydb
POSTGRES_HOST=db
POSTGRES_PORT=5432
NEO4J_URI=bolt://neo4j:7687
NEO4J_USER=neo4j
NEO4J_PASSWORD=your-neo4j-password
GEMINI_API_KEY=your-gemini-api-key
```

Step 3: Start all services

```
cd ../infra
docker compose -f docker-compose.yaml up -d --build
docker compose -f docker-compose-neo4j.yaml up -d --build
```
Step 4: Verify all services

```
# Check running containers
docker ps

# Expected services:
# - api (port 8000)
# - db (port 5433)
# - frontend (port 3000)
# - neo4j (ports 7474, 7687)
# - grafana (port 3001)
```

Step 5: Access Applications

```
Web Frontend: http://localhost:3000
API Documentation: http://localhost:8000/api/schema/swagger-ui/
Neo4j Browser: http://localhost:7474 (neo4j/password)
Grafana: http://localhost:3001 (admin/admin)
```
### 5.2 Local Development Setup (Mobile)

Step 1: Open in Android Studio

```
cd mobile
# Open this directory in Android Studio
```

Step 2: Configure Build Variants

```
Development: Uses staging API (16.171.44.79) (development server api on AWS EC2)
Production: Uses production API (13.60.88.202) (production server api on AWS EC2)
```

Step 3: Build and Run

```
# Debug build
./gradlew :app:assembleProductionDebug

# Run tests
./gradlew :app:testProductionDebugUnitTest
```
## 6. System Configuration
### 6.1 Database Configuration

Postgresql Settings
```
# backend/backend/settings.py
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.postgresql',
        'NAME': os.getenv('POSTGRES_DB'),
        'USER': os.getenv('POSTGRES_USER'),
        'PASSWORD': os.getenv('POSTGRES_PASSWORD'),
        'HOST': os.getenv('POSTGRES_HOST'),
        'PORT': os.getenv('POSTGRES_PORT', 5432),
    }
}
```
Neo4j Configuration

```
NEO4J_URI = os.getenv('NEO4J_URI', 'bolt://neo4j:7687')
NEO4J_USER = os.getenv('NEO4J_USER', 'neo4j')
NEO4J_PASSWORD = os.getenv('NEO4J_PASSWORD')
```

CORS Configuration

```
CORS_ALLOW_ALL_ORIGINS = True  # Development only

CORS_ALLOWED_ORIGINS = [
    "http://localhost:3000",
    "http://54.163.77.79:3000",
    "http://16.171.44.79:3000",
]
```
Grafana Setup

```
# Run setup scripts
cd infra
python3 setup_grafana_dashboard_user.py
python3 setup_grafana_dashboard_space.py
python3 setup_grafana_dashboard_node.py
python3 setup_grafana_cards_analytics.py
```
## 7. Application Features
### 7.1 User Management
### 7.1.1 Registration

Endpoint: POST /api/register/

Features:
* Username validation
* Email verification
* Password strength requirements
* Automatic profile creation

### 7.1.2 Authentication

Endpoint: POST /api/login/

Token Type: JWT (JSON Web Token)

Token Lifetime: Configurable

Refresh Mechanism: Available

### 7.1.3 User Profiles

Endpoint: GET /api/profiles/{username}/

Editable Fields:
* Bio (500 characters max)
* Profession
* Date of Birth
* Location (Country, City)
* Geographic Coordinates

### 7.2 Space Management
### 7.2.1 Create Space

Endpoint: POST /api/spaces/

Required Fields:
* Title (max 200 characters)
* Description
* Tags (optional)
* Location (optional)

### 7.2.2 Join/Leave Space

Join: POST /api/spaces/{id}/join/

Leave: POST /api/spaces/{id}/leave/

Auto-Collaboration: Creator automatically becomes collaborator

### 7.2.3 Space Discovery

Trending: GET /api/spaces/trending/

New Spaces: GET /api/spaces/new/

Search: GET /api/search/?q={query}

### 7.3 Graph Features
### 7.3.1 Node Management

Add Node: POST /api/spaces/{id}/add-node/

View Nodes: GET /api/spaces/{id}/nodes/

Wikidata Integration: Automatic property fetching

Node Properties: Custom key-value pairs

### 7.3.2 Edge Management

Add Edge: POST request with source/target nodes

View Edges: GET /api/spaces/{id}/edges/

Relationship Types: Customizable via Wikidata properties

### 7.3.3 Graph Search

Text Search: GET /api/spaces/{id}/search/text/?q={query}

Property Search: GET /api/spaces/{id}/search/properties/

Advanced Query: POST /api/spaces/{id}/search/query/

### 7.4 Discussion System
### 7.4.1 View Discussions

Endpoint: GET /api/spaces/{id}/discussions/

Access: Public (no authentication required)

Ordering: Chronological

### 7.4.2 Add Discussion

Endpoint: POST /api/spaces/{id}/discussions/add/

Requirement: Must be space collaborator

Validation: Non-empty text required

### 7.4.3 React to Discussions

Endpoint: POST/DELETE /api/spaces/{id}/discussions/{discussion_id}/react/

Values: Upvote (+1), Downvote (-1)

Toggle: Same vote removes reaction

### 7.4.4 Delete Discussion

Endpoint: DELETE /api/spaces/{id}/discussions/{discussion_id}/delete/

Authorization: Admin or Space Moderator only

### 7.5 Reporting System
### 7.5.1 Report Content

Endpoint: POST /api/reports/

Report Types:
* Space
* Discussion
* User
* Report Reasons:
* Spam
* Inappropriate content
* Harassment
* Misinformation

### 7.5.2 Manage Reports (Admin/Moderator)

View Open: GET /api/reports/open/

View Dismissed: GET /api/reports/dismissed/

View Archived: GET /api/reports/archived/

## 8. API Documentation
### 8.1 Authentication Endpoints
| Method | Endpoint        | Description        | Auth Required |
|--------|-----------------|--------------------|---------------|
| POST   | /api/register/  | Register new user  | No            |
| POST   | /api/login/     | Login user         | No            |

### 8.2 Space Endpoints
| Method | Endpoint                                   | Description               | Auth Required |
|--------|--------------------------------------------|---------------------------|---------------|
| GET    | /api/spaces/                               | List all spaces           | Yes           |
| POST   | /api/spaces/                               | Create space              | Yes           |
| GET    | /api/spaces/{id}/                          | Get space details         | Yes           |
| PUT    | /api/spaces/{id}/                          | Update space              | Yes (Owner)   |
| DELETE | /api/spaces/{id}/                          | Delete space              | Yes (Owner)   |
| POST   | /api/spaces/{id}/join/                     | Join space                | Yes           |
| POST   | /api/spaces/{id}/leave/                    | Leave space               | Yes           |
| GET    | /api/spaces/{id}/check-collaborator/       | Check membership          | Yes           |
| GET    | /api/spaces/trending/                     | Get trending spaces       | Yes           |
| GET    | /api/spaces/new/                          | Get newest spaces         | Yes           |

### 8.3 Discussion Endpoints

| Method | Endpoint                                                   | Description             | Auth Required        |
|--------|------------------------------------------------------------|-------------------------|----------------------|
| GET    | /api/spaces/{id}/discussions/                              | List discussions        | No                   |
| POST   | /api/spaces/{id}/discussions/add/                          | Add discussion          | Yes (Collaborator)   |
| POST   | /api/spaces/{id}/discussions/{did}/react/                  | React to discussion     | Yes                  |
| DELETE | /api/spaces/{id}/discussions/{did}/react/                  | Remove reaction         | Yes                  |
| DELETE | /api/spaces/{id}/discussions/{did}/delete/                 | Delete discussion       | Yes (Admin/Mod)      |

### 8.4 Graph Endpoints

| Method | Endpoint                                   | Description             | Auth Required        |
|--------|--------------------------------------------|-------------------------|----------------------|
| POST   | /api/spaces/{id}/add-node/                 | Add node to graph       | Yes (Collaborator)   |
| GET    | /api/spaces/{id}/nodes/                    | Get all nodes           | Yes                  |
| GET    | /api/spaces/{id}/edges/                    | Get all edges           | Yes                  |
| GET    | /api/spaces/{id}/snapshots/                | Get graph snapshots     | Yes                  |
| GET    | /api/spaces/{id}/graph-search/             | Search graph            | Yes                  |

### 8.5 Admin Endpoints

| Method | Endpoint                          | Description               | Auth Required |
|--------|-----------------------------------|---------------------------|---------------|
| POST   | /api/auth/assign-moderator/       | Assign moderator          | Admin         |
| POST   | /api/auth/remove-moderator/       | Remove moderator          | Admin         |
| POST   | /api/auth/change-user-type/       | Change user type          | Admin         |
| GET    | /api/auth/users/                  | List users by type        | Admin         |
| GET    | /api/dashboard/stats/             | Get dashboard stats       | Admin         |

## 9. Database Schema
### 9.1 Core Models

### User (Django Built-in)
* id: Integer (Primary Key)
* username: String (Unique, Max 150)
* email: String (Unique)
* password: Hashed String
* is_staff: Boolean
* is_active: Boolean
* date_joined: DateTime

### Profile
* id: Integer (Primary Key)
* user: ForeignKey → User (One-to-One)
* user_type: Integer (1=Admin, 2=Moderator, 3=User)
* profession: String (Max 100, Optional)
* bio: Text (Max 500, Optional)
* dob: Date (Optional)
* country: String (Max 100, Optional)
* city: String (Max 100, Optional)
* latitude: Float (Optional)
* longitude: Float (Optional)
* location_name: String (Max 255, Optional)
* report_count: Integer (Default 0)
* is_reported: Boolean (Default False)
* is_archived: Boolean (Default False)
* created_at: DateTime
* updated_at: DateTime

### Space
* id: Integer (Primary Key)
* title: String (Max 200)
* description: Text
* creator: ForeignKey → User
* collaborators: ManyToMany → User
* tags: ManyToMany → Tag
* country: String (Max 100, Optional)
* city: String (Max 100, Optional)
* district: String (Max 100, Optional)
* street: String (Max 150, Optional)
* latitude: Float (Optional)
* longitude: Float (Optional)
* report_count: Integer (Default 0)
* is_reported: Boolean (Default False)
* is_archived: Boolean (Default False)
* created_at: DateTime

### Tag
* id: Integer (Primary Key)
* name: String (Max 50, Unique)
* wikidata_id: String (Max 20, Optional)
* wikidata_label: String (Max 100, Optional)

### Node
* id: Integer (Primary Key)
* space: ForeignKey → Space
* wikidata_id: String (Max 20)
* label: String (Max 200)
* description: Text (Optional)
* created_by: ForeignKey → User
* created_at: DateTime

### Edge
* id: Integer (Primary Key)
* space: ForeignKey → Space
* source_node: ForeignKey → Node
* target_node: ForeignKey → Node
* wikidata_property_id: String (Max 20, Optional)
* property_label: String (Max 200, Optional)
* created_by: ForeignKey → User
* created_at: DateTime

### Discussion
* id: Integer (Primary Key)
* space: ForeignKey → Space
* user: ForeignKey → User
* text: Text
* created_at: DateTime

### DiscussionReaction
* id: Integer (Primary Key)
* discussion: ForeignKey → Discussion
* user: ForeignKey → User
* value: Integer (1=Upvote, -1=Downvote)
* created_at: DateTime
* Unique Constraint: (discussion, user)

### 9.2 Neo4j Graph Schema
### Node Structure
```
(:WikidataEntity {
  wikidata_id: String,
  label: String,
  description: String,
  space_id: Integer
})
```
### Property Structure

```
(:WikidataEntity)-[:RELATED_TO {
  property_id: String,
  property_label: String
}]->(:WikidataEntity)
```

## 10. User Roles & Permissions
### 10.1 User Types
### 10.1.1 Regular User (Type 3)
Capabilities:

* Create spaces
* Join/leave spaces
* Add nodes and edges (as collaborator)
* Add discussions (as collaborator)
* React to discussions
* Report content
* Edit own profile

Restrictions:

* Cannot delete others' content
* Cannot access admin dashboard
* Cannot moderate spaces

### 10.1.2 Moderator (Type 2)
Inherited from User, plus:

* Delete discussions in assigned spaces
* Manage reports for assigned spaces
* View moderator dashboard

Restrictions:

* Cannot assign other moderators
* Cannot change user types
* Limited to assigned spaces

### 10.1.3 Admin (Type 1)
Full System Access:

* All user capabilities
* Delete any content
* Assign/remove moderators
* Change user types
* Archive users/spaces
* Access full admin dashboard
* View all reports
* Manage system-wide settings

## 11. Deployment Guide
### 11.1 Development Deployment

```
# Navigate to infrastructure directory
cd infra

# Start all services
docker compose -f docker-compose.yaml up -d --build
docker compose -f docker-compose-neo4j.yaml up -d --build

# Check logs
docker compose -f docker-compose.yaml logs -f api

# Stop services
docker compose -f docker-compose.yaml down
docker compose -f docker-compose-neo4j.yaml down
```
### 11.2 Production Deployment

```
# On production server
cd /home/ubuntu/Connect-The-Dots

# Pull latest code
git pull origin main

# Copy environment file
cp /home/ubuntu/.env backend/.env

# Build and deploy
docker compose -f infra/docker-compose.prod.yaml up -d --build

# Run migrations
docker exec <api-container-name> python manage.py migrate
```

```
# On Neo4j server
cd /home/ubuntu/Connect-The-Dots

# Pull latest code
git pull origin main

# Copy environment file
cp /home/ubuntu/.env backend/.env

# Build and deploy
docker compose -f infra/docker-compose-neo4j.yaml up -d --build

```
### 11.3 Automated Deployment (CI/CD)

Trigger: Push to main or develop branch

Platform: GitHub Actions

Target: AWS EC2 instances

Process:
* Run tests
* Whitelist GitHub runner IP
* SSH into EC2
* Pull latest code
* Rebuild containers
* Run migrations
* Remove runner IP whitelist

## 12. Monitoring & Analytics
### 12.1 Grafana Dashboards

Access: http://localhost:3001 (or production URL:3001)
Credentials: admin/admin

Available Dashboards:

User Analytics

* Daily new user registrations
* Weekly active users
* Monthly user growth
* User distribution by location

Space Analytics

* Space creation trends
* Most active spaces
* Collaborator statistics
* Discussion activity

Node Analytics

* Node creation over time
* Most connected nodes
* Graph growth metrics

### 12.2 Setup Commands

```
cd infra
python3 setup_grafana_dashboard_user.py
python3 setup_grafana_dashboard_space.py
python3 setup_grafana_dashboard_node.py
python3 setup_grafana_cards_analytics.py
```

### 12.3 Application Logs

```
# View API logs
docker logs -f <api-container-name>

# View database logs
docker logs -f <db-container-name>

# View all service logs
docker compose -f infra/docker-compose.yaml logs -f
```

### 12.4 Health Checks

PostgreSQL:

```
docker exec <db-container> pg_isready -U <username>
```

Django API:

```
curl http://localhost:8000/api/
```

Frontend:

```
curl http://localhost:3000
```

Grafana:

```
curl http://localhost:3001/api/health
```

## 13. Maintenance & Updates
### 13.1  Backup Procedures

Database Backup
```
# PostgreSQL backup
docker exec <db-container> pg_dump -U <username> <dbname> > backup_$(date +%Y%m%d).sql

# Restore from backup
docker exec -i <db-container> psql -U <username> <dbname> < backup.sql
```
Volume Backup

```
# Backup Docker volumes
docker run --rm -v postgres_data:/data -v $(pwd):/backup \
  alpine tar czf /backup/postgres_backup.tar.gz /data
```

## 13.2 Update Procedures

```
# Backend
cd backend
pip install --upgrade -r requirements.txt

# Frontend
cd frontend
npm update
```
## 13.3 Update Docker Images

```
# Pull latest base images
docker compose pull

# Rebuild containers
docker compose up -d --build
```


---

# User Manual

## Login & Register & Logout

### **Creating an Account**

1. Navigate to the Connect-The-Dots homepage.
2. Click the "Register" button in the header.
3. Fill in the required fields:
    - Username (must be unique)
    - Email address (must be unique)
    - Password
    - Profession
    - Date of birth (must be at least 18 years old)
    - Location (automatically detected via geolocation, or manually select country and city)
4. If geolocation permission is denied or unavailable, you will be prompted to manually select:
    - Country (from dropdown list)
    - City (from dropdown list, populated based on selected country)
5. Click "Register" to complete the registration.
6. You will see a success message confirming registration is complete.
7. A link to the login page will be displayed if you already have an account.

### **Logging In**

1. Navigate to the Connect-The-Dots homepage.
2. Click the "Login" button in the header.
3. Enter your username and password.
4. Click "Login" button to access your account.
5. Upon successful login, you will be automatically redirected to the home page.
6. Your authentication token and user information will be stored for the session.

### **Logging Out**

1. Click on your username in the header (displayed in the top right corner).
2. A dropdown menu will appear.
3. Click the "Logout" button in the dropdown menu.
4. You will be logged out and redirected to the login page.
5. All stored authentication data will be cleared.

# Node, Edge, Property Create/Update/Delete

### **Adding Nodes from Wikidata**

### **Prerequisites**

- You must be a collaborator of the space to add nodes
- The space must not be archived
- You must be logged in to the system

### **Step-by-Step Instructions**

1. **Navigate to Space Details**
    - Open the space where you want to add a node
    - The "Add Node from Wikidata" section is visible in the left panel (only for collaborators)
2. **Search for a Wikidata Entity**
    - Enter a search term in the search field (e.g., "Albert Einstein", "Paris")
    - Click the "Search" button or press Enter
    - The system will query Wikidata and display matching entities in a dropdown list
3. **Select an Entity**
    - From the dropdown list, select the entity you want to add
    - Each entry shows the entity's label and description (e.g., "Albert Einstein (German theoretical physicist)")
    - Once selected, the entity's available properties will be loaded and displayed
4. **Select Properties**
    - A list of available properties from Wikidata will be displayed
    - Properties are automatically grouped by property ID for better organization
    - Use the search box above the property list to filter properties by name or ID
    - Click on individual properties to select or deselect them
    - Selected properties are highlighted
    - You can use "Select All Properties" checkbox at the top to select/deselect all properties at once
    - You can also select/deselect entire property groups using the group headers
    - Properties show their values (text, numbers, or links to other Wikidata entities)
5. **Optional: Connect to Existing Node**
    - If you want to create a connection (edge) to an existing node:
        - **Select Target Node**: Choose an existing node from the "Connect To Node" dropdown
        - **Set Edge Direction**: Use the direction toggle button to choose:
            - "New → Existing": The new node points to the existing node
            - "Existing → New": The existing node points to the new node
        - **Set Edge Label**:
            - Use the PropertySearch component to search for a Wikidata property
            - Or manually enter a descriptive label for the relationship
            - The label describes the relationship (e.g., "born in", "located in", "author of")
6. **Optional: Add Location Data**
    - Click "Add Location" to expand the location section
    - You can specify location information for the node:
        - **Country**: Select from a dropdown list of countries
        - **City**: Select from a dropdown list (populated based on selected country)
        - **District**: Select from a dropdown list (populated based on selected city)
        - **Street**: Select from a dropdown list (populated based on selected district)
        - **Coordinates**: Manually enter latitude and longitude (optional)
        - **Location Name**: Enter a descriptive location name (optional)
    - Location data is optional and can be added later when editing the node
7. **Create the Node**
    - Click the "Add Node with Edge" button
    - The system will:
        - Create the node with selected properties
        - Create the edge connection if specified
        - Save location data if provided
        - Create a graph snapshot
        - Refresh the graph visualization
    - A success message will confirm the node was added
    - The page will automatically reload to show the new node in the graph
8. **View the New Node**
    - The graph will refresh and display your new node
    - If you created a connection, you'll see an edge between the nodes
    - Click on the node to view its details

### **Notes**

- If you're not a collaborator, you'll see a message prompting you to join the space
- If the space is archived, you cannot add nodes
- You can add a node without connecting it to any existing node (standalone node)
- Properties are fetched from Wikidata and may take a moment to load
- Location data can be added or modified later through node editing

---

### **Editing Node Details**

### **Opening Node Details**

1. **Click on a Node**
    - Click any node in the graph visualization
    - The Node Detail modal will open, displaying comprehensive information about the node

### **Node Information Display**

The modal shows:

- **Node Label**: The name/title of the node
- **Wikidata ID**: If the node is from Wikidata, its unique identifier (clickable link)
- **Description**: Brief description of the entity
- **Node Images**: If available, images from Wikidata are displayed with navigation controls
- **Current Properties**: All properties currently associated with the node, grouped by property ID
- **Location Information**: Country, city, district, street, and coordinates (if available)

### **Viewing Node Images**

- If the node has images from Wikidata, they are displayed in a carousel
- Use arrow buttons or keyboard arrows (← →) to navigate between images
- Images are loaded from Wikimedia Commons

### **Managing Node Properties**

1. **View Current Properties**
    - Properties are displayed in collapsible groups by property ID
    - Each property shows:
        - Property label and ID (e.g., "Date of Birth (P569)")
        - Property value (text, number, or link to another Wikidata entity)
        - Delete button with cross icon to remove individual properties
    - Click on property groups to expand/collapse them
2. **Add or Modify Properties**
    - Scroll to the "Edit Node Properties" section
    - Click the section header to expand it
    - A search box allows you to filter available properties
    - Available properties are displayed in a grouped list:
        - Properties are grouped by property ID
        - Each group can be selected/deselected as a whole
        - Individual properties within groups can be selected/deselect
        - Use "Select All Properties" to select/deselect everything
    - Click on properties to select or deselect them
    - Selected properties are highlighted
    - Click "Save Properties" to apply your changes
    - The node properties will be updated and the graph will refresh
3. **Delete Individual Properties**
    - Find the property you want to remove in the "Current Properties" section
    - Click the delete button with cross icon next to the property
    - The property will be removed from the node

### **Managing Node Location**

1. **View Current Location**
    - Location information is displayed in the node details
    - Shows country, city, district, street, coordinates, and location name (if available)
2. **Edit Location**
    - Click "Edit Location" button to enter edit mode
    - You can modify:
        - **Country**: Select from dropdown (changing country resets city, district, and street)
        - **City**: Select from dropdown (populated based on country, changing city resets district and street)
        - **District**: Select from dropdown (populated based on city, changing district resets street)
        - **Street**: Select from dropdown (populated based on district)
        - **Coordinates**:
            - Manually enter latitude and longitude
            - Or use "Get Coordinates from Address" to automatically geocode the address
            - Or use "Get Address from Coordinates" to reverse geocode coordinates
    - Click "Save Location" to save changes
    - Click "Cancel" to discard changes
    - If you close the modal with unsaved changes, you'll be prompted to save
3. **Geocoding Features**
    - **Forward Geocoding**: Enter address components and click "Get Coordinates from Address" to find latitude/longitude
    - **Reverse Geocoding**: Enter coordinates and click "Get Address from Coordinates" to find address details
    - These features use OpenStreetMap's service

### **Managing Node Connections (Edges)**

1. **View Current Connections**
    - The "Connections" section shows all nodes connected to this node
    - Displays:
        - Connected node labels
        - Edge labels (relationship types)
        - Direction indicators (→)
    - Click on a connected node label to navigate to that node's details
2. **Add a New Connection**
    - Scroll to the "Add Connection" section
    - **Select Target Node**: Choose a node from the dropdown (only shows nodes not already connected)
    - **Set Direction**: Toggle to choose:
        - "Current → Selected": Current node points to selected node
        - "Selected → Current": Selected node points to current node
    - **Set Edge Label**:
        - Use PropertySearch to find a Wikidata property
        - Or enter a custom label describing the relationship
    - Click "Add Connection" to create the edge
    - The connection will appear in the graph and connections list
3. **View Edge Details**
    - Click on any edge in the graph to open the Edge Detail modal
    - See "Managing Edges" section below for details

### **Deleting a Node**

1. **Access Danger Zone**
    - Scroll to the bottom of the Node Detail modal
    - Find the "Danger Zone" section
2. **Delete the Node**
    - Read the warning message about deletion consequences
    - Click the "Delete Node" button
    - The button will change to red colored "Confirm Deletion" button
    - Click "Confirm Deletion" again to permanently delete the node
    - **Warning**: This action:
        - Permanently removes the node
        - Deletes all edges connected to this node
        - Deletes all properties associated with this node
        - Cannot be undone
    - After deletion, the modal closes and the graph refreshes

### **Additional Features**

- **Report Node**: Click the report button to flag inappropriate content
- **Close Modal**: Click the × button or click outside the modal to close
- **Keyboard Navigation**: Use arrow keys to navigate images when available

---

### **Managing Edges (Relationships)**

### **Opening Edge Details**

1. **Click on an Edge**
    - Click any edge (line connecting two nodes) in the graph visualization
    - The Edge Detail modal will open

### **Edge Information Display**

The modal shows:

- **Connected Nodes**:
    - Source node (where the edge starts)
    - Target node (where the edge ends)
    - Both nodes show their labels and IDs
- **Current Edge Label**: The relationship description
- **Wikidata Property ID**: If the edge uses a Wikidata property, its ID is displayed
- **Direction**: Visual indicator showing which node points to which

### **Editing Edge Label and Direction**

1. **Edit Edge Label**
    - Use the PropertySearch component to search for a Wikidata property
    - Or manually type a descriptive label in the text field
    - The label describes the relationship (e.g., "born in", "located in", "author of", "part of")
2. **Change Edge Direction**
    - Click the direction toggle button to reverse the edge direction
    - The button shows the current direction (e.g., "Node A → Node B")
    - Clicking toggles to the opposite direction (e.g., "Node B → Node A")
    - If an edge already exists in the opposite direction, you cannot toggle (duplicate prevention)
    - An error message will appear if a duplicate edge would be created
3. **Save Changes**
    - Click "Update Label & Direction" to save your changes
    - The edge will be updated in the graph
    - The modal will close automatically
    - The graph will refresh to show the updated edge

### **Deleting an Edge**

1. **Access Danger Zone**
    - Scroll to the bottom of the Edge Detail modal
    - Find the "Danger Zone" section
2. **Delete the Edge**
    - Read the warning message
    - Click the "Delete Edge" button
    - The button will change to "Confirm Deletion" (red)
    - Click "Confirm Deletion" again to permanently delete the edge
    - This action permanently removes the connection between the two nodes and cannot be undone
    - After deletion, the modal closes and the graph refreshes

# Space Functionalities

### **Creating a Space**

1. Log in to your Connect-The-Dots account.
2. Click the **Create Space** button located in the header.
3. Fill in the required space information:
    - Space title
    - Description explaining the scope and purpose of the space
    - Location (optional, selectable via map or dropdown)
    - Tags to categorize the space
4. Review the entered details to ensure accuracy.
5. Click **Create Space** to finalize the process.
6. You will be redirected to the newly created space page, where you can begin adding nodes, relationships, and discussions.

---

### **Deleting a Space**

1. Navigate to the space you own or manage.
2. Click the **Space Actions** button in the space header.
3. Select **Delete Space** from the dropdown menu.
4. A confirmation dialog will appear to prevent accidental deletion.
5. Confirm the deletion action.
6. The space will be permanently removed from the system and will no longer be accessible to users.

### **Using AI Summarize**

1. Open the space you want to explore.
2. Click the **Summarize** button located in the space header.
3. The system will collect information from the space, including:
    - Nodes
    - Relationships (edges)
    - Discussion comments
    - User contributions
4. While the summary is being generated, a loading indicator will be displayed.
5. Once completed, the AI-generated summary will appear in a modal window.
6. The summary presents:
    - High-level statistics about the space
    - An overview of the main topics and structure
    - Key entities and their relationships
    - Notable insights derived from the graph and discussions
7. Review the summary to quickly understand the space context.
8. Close the modal to return to the space view without affecting any data.

---

### **Filtering the Space Graph**

1. Open a space and scroll to the **Space Graph** section.
2. Locate the **Filter by Type** panel on the left side of the graph.
3. The panel displays all available *instance of* types present in the graph, such as:
    - Human
    - Work
    - City
    - Building
4. Each type shows a checkbox and a count indicating how many nodes of that type exist in the graph.
5. Select a checkbox to display only the nodes that belong to the chosen type.
6. Deselect a checkbox to hide nodes of that type from the graph.
7. Use **Select All** to display all instance types or **Deselect All** to clear the graph view.
8. The graph updates immediately to reflect the selected filters, allowing focused exploration of specific entity types.
9. The color legend below the filter panel helps users visually distinguish node types in the graph.
10. Filtering does not modify the underlying data and can be adjusted at any time during exploration.

If you want, next we can:

- add the **Advanced Graph Search** manual,
- explain **graph fullscreen mode**, or
- do a **final consistency sweep** across all user manual sections before submission.

---

### **Interacting with the Space Graph**

1. Open a space and scroll to the **Space Graph** section.
2. To view the graph in a larger view, click the **Fullscreen** button located at the top-right corner of the graph area.
3. The graph will expand to fullscreen mode, allowing users to focus entirely on nodes and connections.
4. Click and drag individual nodes to reposition them and better inspect their relationships.
5. Dragging nodes updates edge positions in real time, helping users explore dense or overlapping connections.
6. Use the **arrow controls** located next to the graph to expand the graph container horizontally.
7. Expanding the graph allocates more screen space to the visualization, improving readability for large or complex graphs.
8. Zoom controls allow users to zoom in and out for detailed or high-level exploration.
9. All interactions are visual-only and do not affect the underlying data or graph structure.

# Reporting (User Type 1 - Regular User) 

You can report the followings :

| Content Type | Description                  | Where to Report                                      |
|--------------|------------------------------|------------------------------------------------------|
| Space        | An entire space    | Space detail page → Space Actions button → "Report"        |
| Node         | A graph node          | Node context menu → "Report"                         |
| Discussion   | A comment in a space         | Discussion item → Space Actions button → "Report"          |
| Profile      | A user profile               | Profile page → "Report" button                       |

## 1 Reporting a Space 

1.1 Navigate to the space you want to report.

1.2 Locate the "Space Actions" in the top-right corner of the space details page.

1.3 Click the "Space Actions" menu to open the dropdown.

1.4 Select "Report" from the dropdown menu.

1.5 A report modal will appear with the following information:

* space : Name of the space
* reason: Drop down list

 1.6 From the "Reason for report" dropdown, select one of the following:
*  Commons:
* *   Inappropriate content
* *   Misinformation
* *   Spam
* *   Harassment
* *   Other
*  Space-Specifics:
* *   Duplicate space
* *   Misleading title or description

1.7 Click "Submit Report" to submit your report.

1.8 Wait for the confirmation message: "Thank you for your report. We will review it shortly."

1.9 The modal will automatically close after 1.5 seconds.

## 2. Reporting a Discussion Commnet

2.1 Navigate to the space containing the discussion.

2.2 Scroll to the Discussions section.

2.3 Locate the discussion comment you want to report.

2.4 Click the report button on the right upper corner of the comment in the discussion.

A report modal will appear with the following information:

* discussion: name of the person wrote the comment

* report reason: drop down list

From the "Reason for report" dropdown, select one of the following:
* Commons:
* * Inappropriate content
* * Misinformation
* * Spam
* * Harassment
* * Other
* Discussion-Specifics:
* * Off-topic
* * Offensive language

2.5 Click "Submit Report" to submit your report.

2.6 Wait for the confirmation message: "Thank you for your report. We will review it shortly."

2.7 The modal will automatically close after 1.5 seconds.


## 3 Reporting a Node

3.1 Navigate to the space containing the node.

3.2 View the Graph Visualization or Node List.

3.3 Locate the node you want to report.

3.4 Click on the node to open its detail view.

3.5 Select "Report" from the right corner of the node detail.

A report modal will appear with the following information:

* node: the title of the node

* reason for report: drop down list

From the "Reason for report" dropdown, select one of the following:
* Commons:
* * Inappropriate content
* * Misinformation
* * Spam
* * Harassment
* * Other
* Node-Specifics:
* * Inaccurate information
* * Duplicate node
* * Unverified source

3.6 Click "Submit Report" to submit your report.

3.7 Wait for the confirmation message: "Thank you for your report. We will review it shortly."

3.8 The modal will automatically close after 1.5 seconds.


## 4 Reporting a User

4.1 Navigate to the user's profile page by clicking their username in the space collaborators or from the activity stream.

4.2 Locate the "Report Profile" button on the profile page.

A report modal will appear with the following information:

* profile: user's name

* reason for report: drop down list

From the "Reason for report" dropdown, select one of the following:
* Commons:
* * Inappropriate content
* * Misinformation
* * Spam
* * Harassment
* * Other
* User-Specifics:
* * Fake account
* * Impersonation

4.3 Click "Submit Report" to submit your report.

4.4 Wait for the confirmation message: "Thank you for your report. We will review it shortly."

4.5 The modal will automatically close after 1.5 seconds.

# Managing Reports (Admins & Moderators)

## 1 Accessing the Reports Dashboard

1.1 Log in with an Admin or Moderator account.

1.2 Navigate to the admin dashboard by clicking the admin button in the right upper corner.

1.3 Select "Admin Dashboard" from the drop down list.

1.4 Select "Reports" from the left menu in the admin dashboard.

1.5 The Reports Dashboard will display all open reports grouped by content.

Permission Levels:

* Admins: Can view and manage all reports across the entire platform
* Moderators: Can only manage reports within their assigned spaces
* Moderators cannot dismiss profile reports (Admin-only)

## 2 Viewing Reports

2.1 Default View (Open Reports)

The Reports Dashboard displays Open reports by default.

Reports are grouped by content (same content with multiple reports is shown as one group).

Each report group displays:
* Content Type: Space, Node, Discussion, or Profile
* Content Name/Description: Title  of the reported content
* Number of Reports: Total count of reports for this item
* Report Reasons: List of reasons provided by reporters
* Reporters: Usernames of users who submitted reports
* Date of First Report: When the first report was submitted
* Action Buttons: Dismiss, Archive, or Delete (depending on content type)

For each group of report you can dismiss, archive it, or delete the reported content. Once the report is dismissied, the report content will not be shown in the admin dashboard. If the report is archived, the content can be seen from the Archive from the left menu. (See Archive section for mode details)

# Voting

## 1. Upvoting the Discussion

1.1. Navigate to the space containing the discussion.

1.2. Scroll to the Discussions section.

1.3. Locate the discussion comment you want to upvote.

1.4. Click the upvote button (👍) button next to the discussion.

1.5. The upvote count will increase by 1.

1.6. Toggle: Click the upvote button again to remove your vote.

## Permissions

* Must be authenticated: You must be logged in to vote.
* Anyone can vote: You don't need to be a collaborator of the space.
* One vote per user: You can only cast one vote (up or down) per discussion.

## 2. Downvoting the Discussion

2.1. Navigate to the space containing the discussion.

2.2. Scroll to the Discussions section.

2.3. Locate the discussion comment you want to downvote.

3.4. Click the downvote button (👎) button next to the discussion.

3.5. The downvote count will increase by 1.

3.6. Toggle: Click the downvote button again to remove your vote.

## Permissions:

* Must be authenticated: You must be logged in to vote
* Anyone can vote: You don't need to be a collaborator of the space
* One vote per user: You can only cast one vote (up or down) per discussion

# Discussion

## 1. Adding Comments to Discussion

1.1. Navigate to the space where you want to comment.

1.2. Scroll to the Discussions section.

1.3. Locate the comment input box at the top of the discussions.

1.4. Type your comment in the text field.

1.5. Click the "Post Comment" button.

1.6. Your comment will appear immediately in the discussion list.

1.7. The comment input field will be cleared automatically.

### Permissions:

* Must be authenticated: You must be logged in to comment
* Must be a collaborator: Only space collaborators can add comments
* Non-empty text required: Comments cannot be blank

## 2. Viewing Discussions

2.1. Navigate to any space (authentication not required for viewing).

2.2. Scroll to the Discussions section.

2.3. All discussions are displayed in chronological order (newest first).

2.4. Each discussion shows:
* Author username
* Comment text
* Creation timestamp (date and hour)
* Upvote/downvote counts
* Your current vote (if any)

### Permissions:

* Anyone can view: No authentication required to read discussions
* Public access: Discussions are visible to all users

# Activity Stream

| Activity Type | Description                              | Example                                              |
|---------------|------------------------------------------|------------------------------------------------------|
| Create        | New space, node, or discussion created   | "john_doe created space 'Ancient History'"           |
| Join          | User joined a space                      | "jane_smith joined space 'World Wars'"               |
| Leave         | User left a space                        | "user123 left space 'Physics'"                       |
| Add           | New edge/connection added to graph       | "alice added edge 'Rome' -[capital_of]-> 'Italy'"    |
| Update        | Content modified                         | "bob updated node properties"                        |
| Delete        | Content removed                          | "admin deleted node 'Duplicate Entry'"               |
| Remove        | Property or reaction removed             | "user456 removed reaction"                           |
| Like          | Discussion upvoted                       | "charlie reacted to discussion"                      |
| Dislike       | Discussion downvoted                     | "dana reacted to discussion"                         |

Each activity entry shows:

* Actor: Username of the person who performed the action
* Action Type: What was done (Create, Join, Add, etc.)
* Object: What was affected (Space, Node, Edge, Discussion)
* Timestamp: When the action occurred (Last 1 day)
* Target: Related space or object (if applicable)



## 1. Viewing the Activity Stream

1.1. Log in to your Connect-The-Dots account.

1.2. Navigate to the home page or click any space.

1.3. Locate the Activity Stream section (right side of the home page and space details page).

1.4. Recent activities are displayed in chronological order (newest first).

1.5. Click refresh button to refresh the activity stream.

### Permissions:

Must be authenticated: You must be logged in to view activities

## Archive

| Content Type | Can Be Archived | Effect                                                      |
|--------------|----------------|-------------------------------------------------------------|
| Space        | Yes          | Becomes read-only; no new content can be added              |
| Node         | Yes          | Cannot be modified or deleted                               |
| Profile      | Yes          | User cannot perform any actions                             |
| Discussion   | No           | Use Delete instead                                          |
| Edge         | No           | Indirect (archived when source/target node is archived)     |

### 1 Archiving a Space

1.1 Navigate to the Reports Dashboard in Back Office.

1.2 Locate the space you want to archive from the Reports section.

1.3 Click the "Archive" button.

1.4 Confirm the action if prompted.

1.5 The following occurs automatically:

All open reports for the space are moved to "Archived" status

The space becomes read-only with the following restrictions:
* Cannot add new nodes or edges

* Cannot add new discussions

* Cannot join or leave the space

* Cannot create snapshots

* Cannot update space details

* Cannot delete the space

### Permissions :

Admins: Can archive any space

Moderators: Can archive spaces in their assigned areas only

### 2 Archiving a Node

2.1 Navigate to the Reports from the admin dashboard.

2.2 Locate the node you want to archive(reported).

2.3 Click the "Archive" button (admin/moderator action).

3.4 The following occurs automatically:

All open reports for the node are moved to "Archived" status

The node becomes read-only:
* Cannot be modified or deleted
* Cannot update properties or location
* Edges connected to this node cannot be modified
* Node remains visible but marked as archived

### Permissions : 

Admins: Can archive any node

Moderators: Can archive nodes in their assigned spaces only

### 3 Archiving a Profile

3.1 Navigate to the Reports Dashboard in admin dashboard.

3.2 Locate the profile report you want to archive.

3.3 Click the "Archive" button.

3.4 The following occurs automatically:

All open reports for the profile are moved to "Archived" status

The user account becomes restricted:
* Cannot create spaces
* Cannot join or leave spaces
* Cannot add nodes, edges, or discussions
* Cannot update profile
* Can only view content (read-only access)

# Map
## 1 Accessing the Space Map

1.1 Log in to your Connect-The-Dots account.
1.2 Click "Map" from the left corner of the application.
1.3 An interactive world map appears showing all spaces with location data.
1.4 Each space is represented by a marker/pin on the map.
1.5 Zoom in/out using mouse scroll or +/- buttons.
1.6 Pan the map by clicking and dragging.

What You See:

* Markers: Each pin represents a space
* Clusters: Multiple spaces in close proximity are grouped
* Colors: Different colors may indicate space categories or activity levels
* Popup Info: Click a marker to see space details

## 2 Viewing Nodes on Map

2.1 Navigate to a space that contains nodes with location data.
2.2 Click the "Space Actions" tab then select "Show Space Map".
2.3 An interactive map displays showing all nodes in the space that have coordinates.
2.4 Each node is represented by a marker on the map.
3.5 Select more then 2 node to calculate the distance between the nodes

Node Location Sources:

* Wikidata Properties: Automatically extracted from P625 (coordinate location)
* Manual Entry: User-defined during node creation or update
* Property-based: Extracted from location properties (country, city)

---

# Mobile User Manual

## PROFILE

### Accessing Your Profile
1. Log in to your account with valid credentials
2. From the bottom navigation bar, click the "Profile" tab
3. Your profile screen will be displayed, and you will see the following information:
> - Username
> - Profession
> - Bio
> - Date of Birth
> - Location
> - Join Date
> - Owned Spaces
> - Joined Spaces

### Accessing Other User Profile
1.  Log in to your account with valid credentials
2.  Click one space on the home screen
3.  You will be directed to the space details screen
4.  Click the "Collaborators" 
5.  A pop-up dialog will be shown on screen that contains a list of space collaborators
6.  Click one of them 
7.  The other user's profile will be displayed, and you will see the details of the selected user

### Editing Your Profile Information
1.  Log in to your account with valid credentials
2.  From the bottom navigation bar, click the "Profile" tab
3.  Your profile screen will be displayed
4.  Click the "Edit" button (pencil icon) in the top-right corner
5.  You will be directed to the edit profile screen
6.  Apply your changes to the editable fields
7.  Click the "Save" button to apply changes
8.  You will be directed to the profile page, and the changed values can be seen in the related fields


## AUTHENTICATION

### Login
1.  Open the application
2.  You will see the login screen if you are not logged in already
3.  Enter your username
4.  Enter your password (You can use the eye icon to see the entered password)
5.  Click the "Login" button
6.  If the entered values are correct, you will be directed to the home screen


### Registration
1.  Open the application
2.  You will see the login screen
3.  Click the "Sign Up" text button 
4.  Fill in the registration form with the following information:
> - Email
> - Username
> - Password
> - Profession
> - Date of Birth
> - Country
> - City
5.  Click the "Register" button
6.  If all of the fields are valid, you will be directed to the login screen


### Logout
1.  Log in to your account with valid credentials
2.  Click the "Settings" button in the bottom navigation bar
3.  At the bottom of the page, you will see the logout button
4.  Click the button 
5.  You will be logged out and directed to the login screen



## SPACE MANAGEMENT
It is assumed that you are already logged in to the application for all of the scenarios mentioned under this section


### Create New Space
1.  Click the "Home" button in the bottom navigation bar
2.  Click the "Create Space" button in the bottom-right corner
3.  The space creation screen will appear on the screen
4.  Enter the title of the space
5.  Enter the description of the space
6.  Add relevant tags to the space
> - Click the "Add Tags from Wikidata" button to open the search box
> - Write the tag you want to add
> - Click the "Search" button to search in Wikidata
> - Search results will be listed below the search button
> - Click the add button in one of the options from the result list
7.  If you want to remove the added tag:
> - Click the trash icon button for the added tag
> - The tag will be removed 
8.  Add Location to the space
> - Click the "Add Location" button
> - A pop-up dialog will appear on the screen
> - Select country 
> - Relevant cities will be loaded with respect to the selected country
> - Select city
> - Click the "Save" button to save the location
9. Click the "Create Space" button
10. Space will be created, and you will be directed to the space details screen


### Delete a space
1.  From the bottom navigation bar, click the "Profile" tab
2.  Your profile screen will be displayed
3.  Select one of the spaces from the section "Owned Spaces."
4.  You will be navigated to the space details screen
5.  Click the "three dots" in the top-right corner of the space detail page
6.  The dropdown menu will be displayed with the space options
7.  Select "Delete Space" from the menu
8.  A confirmation pop-up dialog will appear on the screen 
9.  Click "Yes" to confirm the deletion
10. Space will be deleted, and you will be directed to the screen from which you came from


### Joining a Space
1.  Click the "Home" button in the bottom navigation bar
2.  Click on one of the space cards that you are not enrolled in
3.  You will be directed to the space details screen
4.  Click the "Join Space" button
5.  Once you join, you will see the button background color change to red and the text changed as "Leave Space"


### Leaving a Space
1.  Click the "Home" button in the bottom navigation bar
2.  Click on one of the space cards that you are enrolled in
3.  You will be directed to the space details screen
4.  Click the "Leave Space" button
5.  Once you leave the space, you will see the button background color change to green and the text changed as "Join Space"


### Adding a Discussion
1.  Navigate to a space where you are a collaborator
2.  Scroll down to the Discussions section
3.  You will see a text field or text area for adding a new discussion
4.  Write your comment
5.  Click the "Share" button
6.  Your comment will be added at the top of the discussion list


### Voting on a Discussion
1.  Navigate to a space where you are a collaborator
2.  Scroll down to the Discussions section
3.  You will see voting buttons in each discussion card
4.  Select the vote you want to give (up or down vote)
5.  The vote count will be updated, and the vote icon will turn blue
6.  You can remove your vote by clicking the blue vote button



## NODE MANAGEMENT
It is assumed that you are already logged in to the application for all of the scenarios mentioned under this section


### Adding a new Node to a Space
1.  Navigate to a space where you are a collaborator
2.  Click the "See Space Graph" button
3.  Click the "Add New Node" button in the top-right corner
4.  You will be directed to the node creation screen
5.  Enter a value in the "Search Wikidata" text box
6.  Click the search icon to start the search
7.  Select one of the options from the result list as a node entity
8.  Select properties from the property list (optional)
9.  If you want to add a connection with another node
> - Select a node from the dropdown list under the "Connect To Node:" section
> -  Select the direction for the connection
> - Enter a value in the Edge label text box
> - When you type more than three character the search in the Wikidata will start automatically
> - Select one of the options from the result list
10. If you want to add a location to the node
> - Click the "Add Location" button
> - A pop-up dialog will appear on the screen
> - Select country
> -  Select city
> - If you don't know the details, such as longitude and latitude, click the "Get Coordinates from Address" button to retrieve the default values
> - Click the "Save" button to save the location
11. Click the "Create Node" button


### Deleting a Node
1.  Navigate to a space where you are a collaborator
2.  Click the "See Space Graph" button
3.  Click the "See Details" button in one of the nodes
4.  You will be directed to the node details screen
5.  Click the three dots in the top-right corner
6.  Available node options will appear
7.  Select the "Delete Node" button
8.  A confirmation pop-up will appear on screen
9.  Click the "Yes" button
10. Node will be deleted, and you will be directed to the node list screen


### Updating Node Properties
1.  Navigate to a space where you are a collaborator
2.  Click the "See Space Graph" button
3.  Click the "See Details" button in one of the nodes
4.  You will be directed to the node details screen
5.  Scroll down a little bit to see the node properties tab
6.  Select or deselect the properties
7.  Click the "Save Properties" button
8.  Node will be updated, and you will see the newest properties under the Node Properties tab


### Updating Node Location
1.  Navigate to a space where you are a collaborator
2.  Click the "See Space Graph" button
3.  Click the "See Details" button in one of the nodes
4.  You will be directed to the node details screen
5.  Click the "Edit Location" button
6.  A pop-up will appear on the screen to select the location
7.  Select country
8.  Select city
9.  Get the coordinates from the address
10. Click the "Save Changes" button
11. Location will be updated, and you will see the updated location in the node details


## EDGE MANAGEMENT
It is assumed that you are already logged in to the application for all of the scenarios mentioned under this section


### Creating an Edge
1.  Navigate to a space where you are a collaborator
2.  Click the "See Space Graph" button
3.  Click the "See Details" button in one of the nodes
4.  You will be directed to the node details screen
5.  Select the "Connections" tab 
6.  Click the "+" button in the top-right corner
7.  Select the node that you want connect with the current node from the first text box 
8.  Enter a value for the edge label 
9.  The Wikidata search will be done for the entered value
10. Select the direction for the edge
11. Click the "Add Edge" button


### Deleting an Edge
1.  Navigate to a space where you are a collaborator
2.  Click the "See Space Graph" button
3.  Click the "See Details" button in one of the nodes
4.  You will be directed to the node details screen
5.  Select the "Connections" tab 
6.  Click the "See Details" button on one of the edges in the list
7.  Click the three dots in the top-right corner
8.  Available edge options will appear
9.  Select the "Delete Edge" button
10. A confirmation pop-up will appear on screen
11. Click the "Yes" button
12. Edge will be deleted, and you will be directed to the node details screen



## REPORTING
It is assumed that you are already logged in to the application for all of the scenarios mentioned under this section


### Reporting a User Profile
1.  Access to the other user's profile by following the steps in "Accessing Other User Profile" manual
2.  Click the "Report" text button in the top-right corner
3.  A pop-up dialog will appear on screen for you to select the report reason
4.  Click the reason box and select one of the reasons from the list
5.  Click the "Report" button to submit the report


### Reporting a Space
1.  Click the "Home" button in the bottom navigation bar
2.  Click on one of the space cards
3.  You will be directed to the space details screen
4.  Click the three dots in the top-right corner
5.  Space options will appear on the screen
6.  Select the "Report Space" option
7.  A pop-up dialog will appear on screen for you to select the report reason
8.  Click the reason box and select one of the reasons from the list
9.  Click the "Report" button to submit the report


### Reporting a Node
1.  Click the "Home" button in the bottom navigation bar
2.  Click on one of the space cards
3.  You will be directed to the space details screen
4.  Click the "See Space Graph" button
5.  You will be directed to the space node list
6.  Click the "See Details" button in one of the nodes from the list
7.  You will be directed to the node details screen
8.  Click the three dots in the top-right corner
9.  Space options will appear on the screen
10. Select the "Report Node" option
11. A pop-up dialog will appear on screen for you to select the report reason
12. Click the reason box and select one of the reasons from the list
13. Click the "Report" button to submit the report


### Reporting a Discussion
1.  Click the "Home" button in the bottom navigation bar
2.  Click on one of the space cards
3.  You will be directed to the space details screen
4.  Scroll down until you see the discussion section
5.  Click the "Report" button on the discussion card that you want to report
6.  A pop-up dialog will appear on screen for you to select the report reason
7.  Click the reason box and select one of the reasons from the list
8.  Click the "Report" button to submit the report



## SEARCHING
It is assumed that you are already logged in to the application for all of the scenarios mentioned under this section


### Searching Among the Spaces
1.  Click the "Home" button in the bottom navigation bar
2.  You will see a search text box on the home screen
3.  You can type the value you want to search
4.  The spaces will be filtered according to the text you entered, and the results will be listed on screen


### Searching Among the Nodes
1.  Navigate to a space from the home screen
2.  Click the "See Space Graph" button
3.  You will see the list of nodes 
4.  You can use the Search Node text box to search among the nodes


### Searching Among the Edges
1.  Navigate to a space from the home screen
2.  Click the "See Space Graph" button
3.  You will see the list of nodes 
4.  Click "See Details" in one of the nodes from the list
1.  You will be directed to the node details screen
2.  Click the "Connections" tab
3.  You will see the list of connections of the current node
4.  You can use the "Search connections" text box to search among the edges
5.  You can use the Search Node text box to search among the nodes



## ACTIVITY STREAM
It is assumed that you are already logged in to the application for all of the scenarios mentioned under this section


### Viewing Activity Stream
1.  From the bottom navigation bar, click the "Activity Stream" button
2.  You will be directed to the activity stream screen
3.  The activity stream will display recent activities
4.  Activities may include:
> - Node additions
> - Edge creations
> - Discussion posts
> - Space joins/leaves
> - And other space-related activities
5.  If you want to see the related activity, you can click on the activity card to go to the node, space, or user that is mentione din the activity

---

# Test Results
## Automated Unit Tests
### Web Testing
There are 182 unit tests for web application build, some of them can be found at below
<img width="917" height="723" alt="Screenshot from 2025-12-20 20-16-24" src="https://github.com/user-attachments/assets/431e9aaa-6bb2-45de-b65e-f85a3f47c21c" />

### Mobile Testing
There are 304 unit test for mobile application build, some of them can be found at below
<img width="1338" height="794" alt="Screenshot from 2025-12-20 20-21-47" src="https://github.com/user-attachments/assets/3a103a2e-c6f1-4d6f-9117-da8650fdd9ab" />


## Unit Test Coverage Report

| File / Module | Statements | Miss | Coverage | Missing Lines |
|--------------|------------|------|----------|---------------|
| api/__init__.py | 0 | 0 | 100% | - |
| api/admin.py | 24 | 0 | 100% | - |
| api/apps.py | 4 | 0 | 100% | - |
| api/graph.py | 43 | 30 | 30% | 8-9, 12-19, 22-24, 27-31, 34, 37, 40-53, 56-66 |
| api/management/__init__.py | 0 | 0 | 100% | - |
| api/management/commands/__init__.py | 0 | 0 | 100% | - |
| api/management/commands/fetch_missing_p31.py | 111 | 111 | 0% | 14-197 |
| api/management/commands/migrate_to_neo4j.py | 19 | 19 | 0% | 1-37 |
| api/migrations/0001_initial.py | 8 | 8 | 0% | 3-17 |
| api/migrations/0002_tag_wikidata_id_tag_wikidata_label.py | 4 | 4 | 0% | 3-12 |
| api/migrations/0003_graphsnapshot_node_edge.py | 7 | 7 | 0% | 3-16 |
| api/migrations/0004_node_space.py | 5 | 5 | 0% | 3-13 |
| api/migrations/0005_property.py | 5 | 5 | 0% | 3-13 |
| api/migrations/0006_profile_bio_profile_created_at_profile_updated_at_and_more.py | 5 | 5 | 0% | 3-13 |
| api/migrations/0007_discussion.py | 7 | 7 | 0% | 3-16 |
| api/migrations/0008_discussionreaction.py | 6 | 6 | 0% | 1-13 |
| api/migrations/0009_property_statement_id.py | 4 | 4 | 0% | 3-12 |
| api/migrations/0010_remove_null_statement_ids.py | 13 | 13 | 0% | 3-27 |
| api/migrations/0011_profile_user_type_spacemoderator.py | 6 | 6 | 0% | 3-15 |
| api/migrations/0012_edge_wikidata_property_id.py | 4 | 4 | 0% | 3-12 |
| api/migrations/0013_profile_latitude_profile_location_name_and_more.py | 4 | 4 | 0% | 3-12 |
| api/migrations/0014_space_city_space_country_space_district_and_more.py | 4 | 4 | 0% | 3-12 |
| api/migrations/0015_profile_city_profile_country.py | 4 | 4 | 0% | 3-12 |
| api/migrations/0016_node_city_node_country_node_district_node_latitude_and_more.py | 4 | 4 | 0% | 3-12 |
| api/migrations/0017_fix_property_statement_id_constraint.py | 4 | 4 | 0% | 3-12 |
| api/migrations/0018_discussion_is_reported_discussion_report_count_and_more.py | 6 | 6 | 0% | 3-15 |
| api/migrations/0019_edge_created_at_node_created_at.py | 5 | 5 | 0% | 3-13 |
| api/migrations/0020_activity.py | 5 | 5 | 0% | 1-11 |
| api/migrations/0021_archive_and_more.py | 46 | 46 | 0% | 3-85 |
| api/migrations/0022_property_value_fields.py | 4 | 4 | 0% | 1-10 |
| api/migrations/0023_edgeproperty.py | 5 | 5 | 0% | 1-11 |
| api/migrations/0024_alter_edgeproperty_id.py | 4 | 4 | 0% | 3-12 |
| api/migrations/0025_node_description.py | 4 | 4 | 0% | 3-12 |
| api/models.py | 227 | 16 | 93% | 38-42, 45, 102, 106-107, 193, 204, 227-228, 271, 297, 325 |
| api/neo4j_db.py | 195 | 128 | 34% | 24-27, 32-34, 42, 62-63, 71, 79, 93-94, 110-111, 118-129, 136-145, 161-162, 178-179, 197-424 |
| api/permissions.py | 96 | 24 | 75% | 15, 25, 35-43, 51, 65-66, 71, 80-81, 89, 127-128, 141, 146, 148-149 |
| api/reporting.py | 3 | 0 | 100% | - |
| api/serializers.py | 422 | 144 | 66% | 74-111, 180-201, 206-213, 238-279, 304-307, 314, 321-337, 403-404, 427-433, 482-483, 487-488, 492-493, 498-500, 508, 511, 517-518, 523-536, 545, 550, 567, 604, 610, 618, 623, 652-653, 658-659, 664-666 |
| api/views.py | 1720 | 731 | 58% | *multiple ranges* |
| api/wikidata.py | 187 | 130 | 30% | 29-64, 68-82, 91-93, 103, 128, 146-147, 198-235, 240-244, 253-330, 337-400 |
| **TOTAL** | **5081** | **1508** | **70%** | - |

## Manual Tests

### Web Manual Tests

<h2><strong>Test Cases for Registration and Login Functionality</strong></h2>
<h3><strong>Test Scenario 1.1.1: Users shall be able to register to the system with a unique e-mail, unique username, and password.</strong></h3>

Step | Instructions | Expected | Result | Notes
-- | -- | -- | -- | --
1 | Navigate to the application homepage | Registration page is accessible | Pass ✅ | Verify page loads correctly
2 | Click on "Register" button at the bottom | Registration form is displayed | Pass ✅ | Form should show all required fields
3 | Enter a unique username: "testuser123" | Username field accepts input | Pass ✅ | Username should be alphanumeric
4 | Enter a unique email address: "testuser123@example.com" | Email field accepts input | Pass ✅ | Email format should be validated
5 | Enter a password: "TestPass123!” | Password field accepts input | Pass ✅ | Password should be masked
6 | Fill in all other required fields (profession, date of birth, location) | All fields accept input | Pass ✅ | Verify all required fields are present
7 | Click "Register" button | Registration is successful | Pass ✅ | Success message should appear
8 | Attempt to register with the same username | Error message displayed | Pass ✅ | Should show "A user with that username already exists."
9 | Attempt to register with the same email | Error message displayed | Fail ❌ | Should show "Email already exists"
10 | Attempt to register with an invalid email format | Error message displayed | Pass ✅ | Should validate email format


<!-- notionvc: 816e0d6d-8603-4905-8337-5e94b9e3db9c -->## **Test Cases for Registration and Login Functionality**

### **Test Scenario 1.1.1: Users shall be able to register to the system with a unique e-mail, unique username, and password.**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Navigate to the application homepage | Registration page is accessible | Pass ✅ | Verify page loads correctly |
| 2 | Click on "Register" button at the bottom | Registration form is displayed | Pass ✅ | Form should show all required fields |
| 3 | Enter a unique username: "testuser123" | Username field accepts input | Pass ✅ | Username should be alphanumeric |
| 4 | Enter a unique email address: "[[testuser123@example.com](mailto:testuser123@example.com)](mailto:testuser123@example.com)" | Email field accepts input | Pass ✅ | Email format should be validated |
| 5 | Enter a password: "TestPass123!” | Password field accepts input | Pass ✅ | Password should be masked |
| 6 | Fill in all other required fields (profession, date of birth, location) | All fields accept input | Pass ✅ | Verify all required fields are present |
| 7 | Click "Register" button | Registration is successful | Pass ✅ | Success message should appear |
| 8 | Attempt to register with the same username | Error message displayed | Pass ✅ | Should show "A user with that username already exists." |
| 9 | Attempt to register with the same email | Error message displayed | Fail ❌ | Should show "Email already exists" |
| 10 | Attempt to register with an invalid email format | Error message displayed | Pass ✅ | Should validate email format |

### **Test Scenario 1.1.2: As part of the registration, users shall be required to provide their profession/work title and their current location.**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Navigate to registration page | Registration form is displayed | Pass ✅ | Form should be visible |
| 2 | Fill in username, email, and password | Fields accept input | Pass ✅ | Basic fields work correctly |
| 3 | Leave profession field empty | Profession field is required | Pass ✅ | Field should be marked as required |
| 4 | Leave location fields empty | Location is required | Pass ✅ | Location should be required |
| 5 | Enter profession: "Software Engineer” | Profession field accepts input | Pass ✅ | Should accept text input |
| 6 | Select country from dropdown | Country dropdown works | Pass ✅ | Should show list of countries |
| 7 | Select city from dropdown | City dropdown populated based on country | Pass ✅ | Cities should load after country selection |
| 8 | Fill all required fields including profession and location | Form submission allowed | Pass ✅ | Registration should proceed |

### **Test Scenario 1.1.3: The system shall capture location data in a structured GeoLocation format (e.g., latitude, longitude, or standardized place name).**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Navigate to registration page | Registration form is displayed | Pass ✅ | Form should be visible |
| 2 | Allow browser geolocation permission | System attempts to get location | Pass ✅ | Browser should prompt for permission |
| 3 | Grant geolocation permission | Location is automatically filled | Pass ✅ | Latitude/longitude should be captured |
| 4 | Deny geolocation permission | Manual location selection appears | Pass ✅ | Country/city dropdowns should appear |
| 5 | Select country manually: "Turkey" | Country is selected | Pass ✅ | Country value is stored |
| 6 | Select city manually: "Istanbul" | City is selected | Pass ✅ | City value is stored |
| 7 | Complete registration with manual location | Location data is saved | Pass ✅ |  |
| 8 | Complete registration with geolocation | Coordinates are saved | Pass ✅ | Latitude and longitude should be stored |
| 9 | Check user profile after registration | Location is displayed correctly | Pass ✅ | Location should show in structured format |

### **Test Scenario 1.1.5: Users shall be able to log in using their e-mail and password.**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Navigate to the application homepage | Homepage is displayed | Pass ✅ | Page should load correctly |
| 2 | Click on "Login" button | Login form is displayed | Pass ✅ | Login form should appear |
| 3 | Enter registered username: testuser123 | Username field accepts input | Pass ✅ |  |
| 4 | Enter correct password: TestPass123! | Password field accepts input | Pass ✅ | Password should be masked |
| 5 | Click "Login" button | User is logged in successfully | Pass ✅ | Should redirect to home page |
| 6 | Verify user is authenticated | User session is active | Pass ✅ | Token should be stored |
| 7 | Enter incorrect username | Error message displayed | Pass ✅ | Should show "Login failed. Please check your credentials." |
| 8 | Enter incorrect password | Error message displayed | Pass ✅ | Should show "Login failed. Please check your credentials." |
| 9 | Leave username field empty | Form validation error | Pass ✅ | Should show required field error |
| 10 | Leave password field empty | Form validation error | Pass ✅ | Should show required field error |

### **Test Scenario: Logout Functionality**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Log in to the system | User is authenticated | Pass ✅ | User should be logged in |
| 2 | Click on username in header | Dropdown menu appears | Pass ✅ | Menu should show profile, admin (if applicable), logout |
| 3 | Click "Logout" button | User is logged out | Pass ✅ | Should redirect to login page |
| 4 | Verify authentication token is cleared | Token is removed | Pass ✅ | localStorage should be cleared |
| 5 | Verify user cannot access protected pages | Access is denied | Pass ✅ | Should redirect to login |
| 6 | Verify username is cleared from header | Username not displayed | Pass ✅ | Header should show login/register buttons |
| 7 | Verify user role data is cleared | Role data is removed | Pass ✅ | is_staff, is_superuser should be cleared |
| 8 | Attempt to access previous page after logout | Redirected to login | Pass ✅ | Should not allow access |

### **Test Scenario 1.4.2: Users shall be able to add new nodes by connecting them to existing nodes in a space.**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Log in as a registered user: testuser123 | User is authenticated | Pass ✅ | User must be logged in |
| 2 | Navigate to an existing space that user is already joined | Space details page is displayed | Pass ✅ | User should be a collaborator |
| 3 | Verify "Add Node from Wikidata" section is visible | Section is displayed | Pass ✅ | Only visible to collaborators |
| 4 | Enter search term in Wikidata search field: "Albert Einstein" | Search field accepts input | Pass ✅ | Should accept text input |
| 5 | Click "Search" button | Search results are displayed | Pass ✅ | Results should appear in dropdown |
| 6 | Select entity “Albert Einstein (German-born theoretical physicist)” from search results | Entity is selected | Pass ✅ | Entity properties should load |
| 7 | Select properties from available list | Properties are selected | Pass ✅ | Multiple properties can be selected |
| 8 | Select an existing node from "Connect To Node" dropdown | Node is selected | Pass ✅ | Should show all existing nodes |
| 9 | Enter edge label: “born in” | Edge label field accepts input | Pass ✅ | Should accept text or property search |
| 10 | Set edge direction (new → existing or existing → new) | Direction is set | Pass ✅ | Toggle button should work |
| 11 | Click "Add Node with Edge" button | Node and edge are created | Pass ✅ | Success message should appear |
| 12 | Verify graph refreshes | New node appears in graph | Pass ✅ | Graph should update automatically |
| 13 | Verify edge connects new node to existing node | Edge is visible | Pass ✅ | Connection should be displayed |
| 14 | Add node without connecting to existing node | Standalone node is created | Pass ✅ | Node should appear without edge |
| 15 | Verify node appears in node list | Node is listed | Pass ✅ | Should appear in space node list |

### **Test Scenario 1.4.5: A user shall be able to add, delete, or modify nodes in a joined space.**

### **Sub-test: Adding Nodes**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Log in as a collaborator of a space: testuser123 | User is authenticated and has access | Pass ✅ | User must be space collaborator |
| 2 | Navigate to space details page | Space page is displayed | Pass ✅ | Graph should be visible |
| 3 | Add a new node following steps from 1.4.2 | Node is created | Pass ✅ | Node creation should work |
| 4 | Verify node count increases | Node count updates | Pass ✅ | Count should increment |
| 5 | Verify activity stream logs node addition | Activity is logged | Pass ✅ | Should show in activity stream |

### **Sub-test: Modifying Nodes**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Click on an existing node(Michelangelo) in the graph | Node detail modal opens | Pass ✅ | Modal should display node info |
| 2 | Verify node information is displayed | Node details are shown | Pass ✅ | Label, Wikidata ID, properties visible |
| 3 | Expand "Edit Node Properties" section | Section expands | Pass ✅ | Available properties should load |
| 4 | Search for a property using search box: image | Properties are filtered | Pass ✅ | Search should work in real-time |
| 5 | Select additional properties: image | Properties are selected | Pass ✅ | Multiple properties can be selected |
| 6 | Click "Save Properties" button | Properties are updated | Pass ✅ | Success message should appear |
| 7 | Verify graph refreshes | Node updates in graph | Pass ✅ | Graph should reflect changes |
| 8 | Verify updated properties appear in node details | Image property should be visible | Pass ✅ |  |
| 9 | Edit node location | Location editing interface appears | Pass ✅ | Should allow location modification |
| 10 | Update location fields | Location is updated | Pass ✅ | Changes should be saved |
| 11 | Verify activity stream logs modification | Activity is logged | Pass ✅ | Should show update activity |

### **Sub-test: Deleting Nodes**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Click on an existing node in the graph: Medici Bank node in The High Renaissance and Rivalries Within space | Node detail modal opens | Pass ✅ | Modal should display |
| 2 | Scroll to "Danger Zone" section | Section is visible | Pass ✅ | Should be at bottom of modal |
| 3 | Check warning message | Warning is displayed | Pass ✅ | Should explain deletion consequences |
| 4 | Click "Delete Node" button | Button changes to "Confirm Deletion" | Pass ✅ | Two-step confirmation process |
| 5 | Click "Click again to confirm deletion" button | Node is deleted | Pass ✅ | Success message should appear |
| 6 | Verify node is removed from graph | Node disappears | Pass ✅ | Graph should update immediately |
| 7 | Verify all edges connected to node are deleted | Edges are removed | Pass ✅ | Connected edges should be deleted |
| 8 | Verify node count decreases | Node count updates | Pass ✅ | Count should decrement |
| 9 | Verify activity stream logs deletion | Activity is logged | Pass ✅ | Should show deletion activity |
| 10 | Attempt to delete node as non-collaborator | Access denied | Pass ✅ | Should show permission error |
| 11 | Attempt to delete node in archived space | Operation prevented | Pass ✅ | Should show archived space error |

### **Test Scenario 1.4.6: A user shall be able to add or remove a connection between nodes in a joined space.**

### **Sub-test: Adding Connections (Edges)**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Log in as a collaborator: testuser123 | User is authenticated | Pass ✅ | User must have access |
| 2 | Navigate to space with at least 2 nodes: Chicago Bulls History | Space page is displayed | Pass ✅ | Need multiple nodes to connect |
| 3 | Click on a node in the graph: Bad Boys | Node detail modal opens | Pass ✅ | Modal should display |
| 4 | Scroll to "Add Connection" section | Section is visible | Pass ✅ | Should show connection options |
| 5 | Select a target node(Michael Jordan) from dropdown | Node is selected | Pass ✅ | Should only show unconnected nodes |
| 6 | Set edge direction (current → selected or selected → current) | Direction is set | Pass ✅ | Toggle should work |
| 7 | Enter or search for edge label: rival of | Edge label is set | Pass ✅ | Should accept text or property search |
| 8 | Click "Add Connection" button | Edge is created | Pass ✅ | Success message should appear |
| 9 | Verify edge “rival of” appears in graph | Edge is visible | Pass ✅ | Connection should be displayed |
| 10 | Click on the edge in graph | Edge detail modal opens | Pass ✅ | Should show edge information |
| 11 | Verify activity stream logs edge creation | Activity is logged | Pass ✅ | Should show in activity stream |
| 12 | Attempt to create duplicate edge | Can not select same two nodes | Pass ✅ | Should prevent duplicates |

### **Sub-test: Removing Connections (Edges)**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Click on an existing edge “rival of” in the graph “Chicago Bulls History” | Edge detail modal opens | Pass ✅ | Modal should display edge info |
| 2 | Verify edge information is displayed | Source and target nodes shown | Pass ✅ | Should show connected nodes |
| 3 | Scroll to "Danger Zone" section | Section is visible | Pass ✅ | Should be at bottom |
| 4 | Check warning message | Warning is displayed “Deleting this edge will remove the connection between these nodes. This action cannot be undone.” | Pass ✅ | Should explain deletion consequences |
| 5 | Click "Delete Edge" button | Button changes to "Click again to confirm deletion" | Pass ✅ | Two-step confirmation |
| 6 | Click Confirm Deletion button | Edge is deleted | Pass ✅ | Success message should appear |
| 7 | Verify edge is removed from graph | Edge disappears | Pass ✅ | Graph should update |
| 8 | Verify nodes remain in graph | Nodes are still present | Pass ✅ | Only edge should be deleted |
| 9 | Verify activity stream logs deletion | Activity is logged | Pass ✅ | Should show deletion activity |

### **Test Scenario 1.4.7: A user shall be able to create and edit properties associated with nodes within a joined space.**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Click on node “Michael Jordan” in the graph “Chicago Bulls History” | Node detail modal opens | Pass ✅ | Modal should display |
| 2 | View "Node Properties" section | Properties are displayed | Pass ✅ | Should show existing properties |
| 3 | Verify properties are grouped by property ID | Properties are grouped | Pass ✅ | Should show grouped organization |
| 4 | Expand "Edit Node Properties" section | Section expands | Pass ✅ | Available properties should load |
| 5 | Search for property: date of birth | Properties are filtered | Pass ✅ | Search should filter in real-time |
| 6 | Select property “date of birth” from available list | Property is selected | Pass ✅ | Should highlight when selected |
| 7 | Select multiple properties | Multiple properties selected | Pass ✅ | Should allow multi-select |
| 8 | Click "Save Properties" button | Properties are added | Pass ✅ | Success message should appear |
| 9 | Verify new properties appear in "Node Properties" | Properties are displayed | Pass ✅ | Should show in grouped format |
| 10 | Click delete button (×) next to “date of birth” property | Property deletion confirmation | Pass ✅ | Property should disappear |
| 11 | Verify graph refreshes after property changes | Graph updates | Pass ✅ | Graph should reflect changes |
| 12 | Verify activity stream logs property changes | Activity is logged | Pass ✅ | Should show update activity |

### **Test Scenario 1.4.8: The system shall allow users to select and modify properties from available Wikidata properties when creating or editing nodes.**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Navigate to "Add Node from Wikidata" section in space “Chicago Bulls History” | Section is displayed | Pass ✅ | Should be visible to collaborators |
| 2 | Search and select “Dennis Rodman” Wikidata entity | Entity properties load | Pass ✅ | Properties should fetch from Wikidata |
| 3 | Verify available properties are displayed | Properties list appears | Pass ✅ | Should show all available properties |
| 4 | Verify properties show property labels and IDs | Labels and IDs visible | Pass ✅ | Format: "Label (P###)" |
| 5 | Verify properties show their values | Property values displayed | Pass ✅ | Should show text, numbers, or entity links |
| 6 | Select properties “member of sports team”, “place of birth” and “country of citizenship” during node creation | Properties are selected | Pass ✅ | Multiple selection should work |
| 7 | Create node with selected properties | Node is created with properties | Pass ✅ | Properties should be saved |
| 8 | Click on created node | Node detail modal opens | Pass ✅ | Should show node information |
| 9 | Verify selected properties are displayed under “Node Properties” | Properties are shown | Pass ✅ | Should match selected properties |
| 10 | Expand "Edit Node Properties" section | Section expands | Pass ✅ | Available properties should load |
| 11 | Verify additional Wikidata properties are available | More properties shown | Pass ✅ | Should show all Wikidata properties |
| 12 | Select additional property “educated at” | Properties are selected | Pass ✅ | Should allow adding more |
| 13 | Click "Save Properties" | Properties are updated | Pass ✅ | New properties should be added |
| 14 | Verify property values are correctly formatted | Values display correctly | Pass ✅ | Text, numbers, entity links should work |
| 15 | Verify entity property values are clickable links | Links are clickable | Pass ✅ | Should link to Wikidata pages |

### **Test Scenario 1.4.9: The system shall group node properties by their property ids for improved organization and display.**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Click on node “Dennis Rodman” with multiple properties | Node detail modal opens | Pass ✅ | Modal should display |
| 2 | View "Node Properties" section | Should show all added properties | Pass ✅ |  |
| 3 | Verify properties are grouped by property ID | Properties are grouped | Pass ✅ | Same property IDs should be grouped |
| 4 | Verify each group shows property label and ID | Group headers visible | Pass ✅ | Format: "Label (P###)" |
| 5 | Verify multiple values for same property are grouped | Values are grouped | Pass ✅ | Same property should show all values |
| 6 | Expand a property group | Group expands | Pass ✅ | Should show all values in group |
| 7 | Collapse a property group | Group collapses | Pass ✅ | Should hide values |
| 8 | View "Edit Node Properties" section | Available properties shown | Pass ✅ | Should display available properties |
| 9 | Verify available properties are also grouped | Properties are grouped | Pass ✅ | Should use same grouping logic |
| 10 | Verify group headers allow select/deselect all | Group selection works | Pass ✅ | Should select all values in group |
| 11 | Select property group “field of work” | All properties in group selected: “wrestling”, “basketball”, “acting” | Pass ✅ | Should highlight all group items |
| 12 | Verify "Select All Properties" works | All properties selected | Pass ✅ | Should select across all groups |

### **Test Scenario 1.4.10: The system shall visually distinguish nodes by applying color coding based on their instance type groups.**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Navigate to space “The High Renaissance and Rivalries Within” with multiple nodes | Space graph is displayed | Pass ✅ | Graph should show nodes |
| 2 | Verify nodes have different colors based on instance types | Nodes are color-coded | Pass ✅ | Different instance types should have different colors |
| 3 | Check node with instance type "Human" | Node has specific color blue | Pass ✅ | Should match Human color scheme |
| 4 | Check node with instance type "City" | Node has different color orange | Pass ✅ | Should match City color scheme |
| 5 | Check node with instance type "Work" | Node has different color pink | Pass ✅ | Should match Work color scheme |
| 6 | Click on a node | Node detail modal opens | Pass ✅ | Modal should display |
| 7 | Verify instance type information is displayed | Instance type shown | Pass ✅ | Should show type and group |
| 8 | Verify color matches instance type group | Color is consistent | Pass ✅ | Color should match group definition |
| 9 | Verify color coding is accessible | Colors are distinguishable | Pass ✅ | Should work for color-blind users |
| 10 | Check instance type legend  | Legend is displayed | Pass ✅ | Should show color mappings |

### **Test Scenario 1.4.11: The system shall provide filtering functionality that allows users to filter nodes by their instance type.**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Navigate to space “The High Renaissance and Rivalries Within” with multiple node types | Space graph is displayed | Pass ✅ | Graph should show various node types |
| 2 | Locate instance type filter component on left side of graph | Filter is visible | Pass ✅ | Should be accessible in UI |
| 3 | Select "Human" instance type filter | Filter is applied and highlights only Human nodes | Pass ✅ | Should show only Human nodes |
| 4 | Verify filtered node count is displayed | Count is shown,  “9/22 nodes" | Pass ✅ | - |
| 5 | Select multiple instance types | Multiple filters applied | Pass ✅ | Should highlight nodes of selected types |
| 6 | Deselect all filters | Should highlight all nodes again | Pass ✅ | - |
| 7 | Verify graph updates dynamically | Graph refreshes, should update without page reload | Pass ✅ | - |
| 8 | Verify edge visibility with filtered nodes | Edges adjust | Pass ✅ | Edges should connect visible nodes |

### **Test Scenario 1.4.12: The system shall provide a full-screen mode for the space graph visualization.**

| Step | Instructions | Expected | Result | Notes |
| --- | --- | --- | --- | --- |
| 1 | Navigate to space “The High Renaissance and Rivalries Within” | Space graph is displayed | Pass ✅ | Graph should be visible |
| 2 | Locate "Fullscreen" button | Button is visible on top right of graph | Pass ✅ | - |
| 3 | Click "Fullscreen" button | Graph enters fullscreen mode | Pass ✅ | - |
| 5 | Verify graph controls are accessible in fullscreen | Zoom, pan should work | Pass ✅ | - |
| 6 | Verify nodes and edges are visible | Content is displayed, all elements should be visible | Pass ✅ | - |
| 7 | Click "Exit Fullscreen" button | Fullscreen exits and should return to regular view | Pass ✅ | - |
| 8 | Press Escape key | Fullscreen exits | Pass ✅ | - |
| 10 | Verify no data is lost in fullscreen | All nodes/edges should remain | Pass ✅ | - |

### Test Scenarios for Space operations

<h2>1. Creating a Space should create the space and redirect the user to the space details page</h2>

STEP | EXPECTED BEHAVIOR | RESULT
-- | -- | --
Open the application | Feed page is displayed | –
Log in with a valid account | User is logged in and header shows user menu | –
Click the Create Space button in the header | Create Space form/modal is displayed | –
Fill in the required fields (title, description, tags and location if applicable) | Form accepts inputs and required fields are validated | –
Click the Create Space button | Space is created successfully | –
Observe the page after creation | User is redirected to the newly created space’s details page | –
Verify the space appears in lists | Space appears in Feed/My Spaces | Passed

<h2>2. Creating a Space with missing required fields should show validation errors and block submission</h2>

STEP | EXPECTED BEHAVIOR | RESULT
-- | -- | --
Open the Create Space form | Create Space form is displayed | –
Leave a required field empty (title) | Field remains empty | –
Click Create Space button | Submission is blocked | –
Observe validation message | Fill out the field message is displayed for the missing fields | –
Fill the missing required fields | Error message disappears | –
Click Create Space again | Space creation proceeds successfully | Passed

<h2>3. Deleting a Space should remove it from the application and space lists</h2>

STEP | EXPECTED BEHAVIOR | RESULT
-- | -- | --
Log in with the space owner account | User is logged in | –
Open the space details page for a space owned by the user | Space details page is displayed | –
Open Space Actions dropdown | Dropdown menu is displayed | –
Click Delete Space | Confirmation dialog is displayed | –
Confirm deletion (click Delete) | Space is deleted successfully | –
Observe the page after deletion | User is redirected to Feed page | –
Search for the deleted space in Feed/My Spaces | Space is no longer visible in listings | Passed

<h2>4. Deleting a Space should require confirmation and cancel should keep the space unchanged</h2>

STEP | EXPECTED BEHAVIOR | RESULT
-- | -- | --
Log in with the space owner account | User is logged in | –
Open a space details page owned by the user | Space details page is displayed | –
Open Space Actions dropdown and click Delete Space | Confirmation dialog is displayed | –
Click Cancel the dialog | Dialog closes | –
Verify the space still exists | User remains on the same space page and the space is unchanged | Passed

<h2>5. AI Summary should generate and display a formatted summary for a space</h2>

STEP | EXPECTED BEHAVIOR | RESULT
-- | -- | --
Log in with a valid account | User is logged in | –
Open a space details page | Space details page is displayed | –
Click the Summarize button | Loading state is shown | –
Wait until generation completes | Summary modal opens automatically | –
Review the summary content | Summary displays structured sections and includes key statistics | –
Close the summary modal | Modal closes and user returns to space page | Passed

<h2>6. AI Summary should handle errors gracefully and keep the page usable</h2>

STEP | EXPECTED BEHAVIOR | RESULT
-- | -- | --
Open a space details page | Space details page is displayed | –
Trigger AI summary while API is unavailable | Request fails | –
Observe UI feedback | User-friendly error message is shown | –
Verify page stability | Space page remains usable; no crash or broken UI | –
Click close on the error message | Error dialog closes, user stays on the space page | Passed

<h2>7. Fullscreen Graph should open and close correctly</h2>

STEP | EXPECTED BEHAVIOR | RESULT
-- | -- | --
Open a space and scroll to Space Graph | Graph is displayed | –
Click the Fullscreen button | Graph opens in fullscreen mode | –
Verify visibility | Graph takes the full screen and is readable | –
Exit fullscreen | Fullscreen closes | –
Verify state after exit | User returns to the space page at the graph section | Passed

<h2>8. Dragging nodes should reposition them and keep edges connected correctly</h2>

STEP | EXPECTED BEHAVIOR | RESULT
-- | -- | --
Open a space and locate the graph | Graph is displayed | –
Click and drag a node to a new position | Node moves smoothly | –
Observe connected edges during drag | Edges stay attached to the node and update in real time | –
Release the node | Node remains in the new position | –
Continue exploring | Graph remains responsive and stable | Passed

<h2>9. Expanding the graph container using side arrows should increase graph viewing space</h2>

STEP | EXPECTED BEHAVIOR | RESULT
-- | -- | --
Open a space and scroll to Space Graph | Graph is displayed | –
Click the arrow control next to the graph to expand the graph area | Graph container expands and takes more space | –
Verify readability | More of the graph is visible without horizontal crowding | –
Click the arrow again to collapse | Graph container returns to default size | –
Verify UI layout | Sidebar/other panels render correctly without overlap | Passed

### GRAPH SEARCH
#### Test Environment
- **URL**: http://13.60.88.202:3000/spaces/8
- **Space title**: The High Renaissance and Rivalries Within
- **Prerequisites**: Logged in user who can open Space #8
- **Test Credentials**: username: `batuhancomert`, password: `12345678`

| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| In "Advanced Graph Search", click the "Select Nodes" dropdown | The node dropdown opens | - |
| In the "Search nodes..." input, type "Leonardo" | The list shows "Leonardo da Vinci" | - |
| Check the checkbox for "Leonardo da Vinci" | "Leonardo da Vinci" is selected | - |
| Click outside the dropdown to close it | The node dropdown closes | - |
| In "Relation Depth", select "1 Level" | "Relation Depth" is set to "1 Level" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (10)" and "Edges (14)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |

#### Expected Nodes (10)
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 48: Lorenzo de' Medici
- 50: Mona Lisa
- 51: The Last Supper
- 59: High Renaissance
- 61: Ginevra de' Benci
- 62: cheese

#### Expected Edges (14)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 37: Michelangelo -[rival of]-> Leonardo da Vinci
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 40: Michelangelo -[rival of]-> Raphael
- 41: Lorenzo de' Medici -[sponsor]-> Leonardo da Vinci
- 42: Lorenzo de' Medici -[sponsor]-> Michelangelo
- 43: Lorenzo de' Medici -[sponsor]-> Raphael
- 49: Leonardo da Vinci -[part of]-> High Renaissance
- 50: Michelangelo -[part of]-> High Renaissance
- 51: Raphael -[part of]-> High Renaissance
- 52: Leonardo da Vinci -[creator]-> Mona Lisa
- 53: Leonardo da Vinci -[creator]-> The Last Supper
- 61: Leonardo da Vinci -[tried new techniques with]-> Ginevra de' Benci
- 62: Leonardo da Vinci -[manufacturer]-> cheese

#### Nodes With Yellow Tick Badge
- 42: Leonardo da Vinci


| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| In "Advanced Graph Search", click the "Select Nodes" dropdown | The node dropdown opens | - |
| In the "Search nodes..." input, type "Leonardo" | The list shows "Leonardo da Vinci" | - |
| Check the checkbox for "Leonardo da Vinci" | "Leonardo da Vinci" is selected | - |
| Click outside the dropdown to close it | The node dropdown closes | - |
| In "Relation Depth", select "2 Levels" | "Relation Depth" is set to "2 Levels" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (16)" and "Edges (21)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |

#### Expected Nodes (16)
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 47: Julius II
- 48: Lorenzo de' Medici
- 49: House of Medici
- 50: Mona Lisa
- 51: The Last Supper
- 55: Sistine Madonna
- 59: High Renaissance
- 60: workshop
- 61: Ginevra de' Benci
- 63: Donato Bramante
- 66: Leo XIII

#### Expected Edges (21)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 37: Michelangelo -[rival of]-> Leonardo da Vinci
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 39: Raphael -[student of]-> Pietro Perugino
- 40: Michelangelo -[rival of]-> Raphael
- 41: Lorenzo de' Medici -[sponsor]-> Leonardo da Vinci
- 42: Lorenzo de' Medici -[sponsor]-> Michelangelo
- 43: Lorenzo de' Medici -[sponsor]-> Raphael
- 44: Julius II -[sponsor]-> Michelangelo
- 45: Julius II -[sponsor]-> Raphael
- 46: Lorenzo de' Medici -[member of]-> House of Medici
- 49: Leonardo da Vinci -[part of]-> High Renaissance
- 50: Michelangelo -[part of]-> High Renaissance
- 51: Raphael -[part of]-> High Renaissance
- 52: Leonardo da Vinci -[creator]-> Mona Lisa
- 53: Leonardo da Vinci -[creator]-> The Last Supper
- 59: Sistine Madonna -[creator]-> Raphael
- 60: Andrea del Verrocchio -[has a]-> workshop
- 61: Leonardo da Vinci -[tried new techniques with]-> Ginevra de' Benci
- 63: Raphael -[student of]-> Donato Bramante
- 66: Raphael -[patron of]-> Leo XIII

#### Nodes With Yellow Tick Badge
- 42: Leonardo da Vinci

| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| Click the "Select Edge Types" dropdown | The edge types dropdown opens | - |
| In the "Search edge types..." input, type "student" | The list shows "student of" | - |
| Check the checkbox for "student of" | "student of" is selected | - |
| Click outside the dropdown to close it | The edge types dropdown closes | - |
| In "Relation Depth", select "1 Level" | "Relation Depth" is set to "1 Level" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (5)" and "Edges (4)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |

#### Expected Nodes (5)
- 42: Leonardo da Vinci
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 63: Donato Bramante

#### Expected Edges (4)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 39: Raphael -[student of]-> Pietro Perugino
- 63: Raphael -[student of]-> Donato Bramante

#### Nodes With Yellow Tick Badge
- None

#### Matched (Animated) Edges
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 39: Raphael -[student of]-> Pietro Perugino
- 63: Raphael -[student of]-> Donato Bramante


| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| Click the "Select Edge Types" dropdown | The edge types dropdown opens | - |
| In the "Search edge types..." input, type "student" | The list shows "student of" | - |
| Check the checkbox for "student of" | "student of" is selected | - |
| Click outside the dropdown to close it | The edge types dropdown closes | - |
| In "Relation Depth", select "2 Levels" | "Relation Depth" is set to "2 Levels" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (16)" and "Edges (22)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |

#### Expected Nodes (16)
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 47: Julius II
- 48: Lorenzo de' Medici
- 50: Mona Lisa
- 51: The Last Supper
- 55: Sistine Madonna
- 59: High Renaissance
- 60: workshop
- 61: Ginevra de' Benci
- 63: Donato Bramante
- 64: St. Peter's Basilica
- 66: Leo XIII

#### Expected Edges (22)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 37: Michelangelo -[rival of]-> Leonardo da Vinci
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 39: Raphael -[student of]-> Pietro Perugino
- 40: Michelangelo -[rival of]-> Raphael
- 41: Lorenzo de' Medici -[sponsor]-> Leonardo da Vinci
- 42: Lorenzo de' Medici -[sponsor]-> Michelangelo
- 43: Lorenzo de' Medici -[sponsor]-> Raphael
- 44: Julius II -[sponsor]-> Michelangelo
- 45: Julius II -[sponsor]-> Raphael
- 49: Leonardo da Vinci -[part of]-> High Renaissance
- 50: Michelangelo -[part of]-> High Renaissance
- 51: Raphael -[part of]-> High Renaissance
- 52: Leonardo da Vinci -[creator]-> Mona Lisa
- 53: Leonardo da Vinci -[creator]-> The Last Supper
- 59: Sistine Madonna -[creator]-> Raphael
- 60: Andrea del Verrocchio -[has a]-> workshop
- 61: Leonardo da Vinci -[tried new techniques with]-> Ginevra de' Benci
- 63: Raphael -[student of]-> Donato Bramante
- 64: Donato Bramante -[architect]-> St. Peter's Basilica
- 65: St. Peter's Basilica -[commissioned by]-> Julius II
- 66: Raphael -[patron of]-> Leo XIII

#### Nodes With Yellow Tick Badge
- None

#### Matched (Animated) Edges
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 39: Raphael -[student of]-> Pietro Perugino
- 63: Raphael -[student of]-> Donato Bramante


| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| Click the "Select Properties" dropdown | The properties dropdown opens | - |
| In the "Search properties..." input, type "P106" | The list shows property "P106" | - |
| Check the checkbox for "P106" | Property "P106" is selected | - |
| Click outside the dropdown to close it | The properties dropdown closes | - |
| In "Relation Depth", select "1 Level" | "Relation Depth" is set to "1 Level" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (17)" and "Edges (23)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |


#### Expected Nodes (17)
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 47: Julius II
- 48: Lorenzo de' Medici
- 49: House of Medici
- 50: Mona Lisa
- 51: The Last Supper
- 55: Sistine Madonna
- 59: High Renaissance
- 60: workshop
- 61: Ginevra de' Benci
- 63: Donato Bramante
- 64: St. Peter's Basilica
- 66: Leo XIII

#### Expected Edges (23)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 37: Michelangelo -[rival of]-> Leonardo da Vinci
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 39: Raphael -[student of]-> Pietro Perugino
- 40: Michelangelo -[rival of]-> Raphael
- 41: Lorenzo de' Medici -[sponsor]-> Leonardo da Vinci
- 42: Lorenzo de' Medici -[sponsor]-> Michelangelo
- 43: Lorenzo de' Medici -[sponsor]-> Raphael
- 44: Julius II -[sponsor]-> Michelangelo
- 45: Julius II -[sponsor]-> Raphael
- 46: Lorenzo de' Medici -[member of]-> House of Medici
- 49: Leonardo da Vinci -[part of]-> High Renaissance
- 50: Michelangelo -[part of]-> High Renaissance
- 51: Raphael -[part of]-> High Renaissance
- 52: Leonardo da Vinci -[creator]-> Mona Lisa
- 53: Leonardo da Vinci -[creator]-> The Last Supper
- 59: Sistine Madonna -[creator]-> Raphael
- 60: Andrea del Verrocchio -[has a]-> workshop
- 61: Leonardo da Vinci -[tried new techniques with]-> Ginevra de' Benci
- 63: Raphael -[student of]-> Donato Bramante
- 64: Donato Bramante -[architect]-> St. Peter's Basilica
- 65: St. Peter's Basilica -[commissioned by]-> Julius II
- 66: Raphael -[patron of]-> Leo XIII

#### Nodes With Yellow Tick Badge
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 48: Lorenzo de' Medici
- 63: Donato Bramante


| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| Click the "Select Properties" dropdown | The properties dropdown opens | - |
| In the "Search properties..." input, type "P106" | The list shows property "P106" | - |
| Check the checkbox for "P106" | Property "P106" is selected | - |
| Click outside the dropdown to close it | The properties dropdown closes | - |
| In "Relation Depth", select "2 Levels" | "Relation Depth" is set to "2 Levels" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (21)" and "Edges (29)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |


#### Expected Nodes (21)
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 47: Julius II
- 48: Lorenzo de' Medici
- 49: House of Medici
- 50: Mona Lisa
- 51: The Last Supper
- 52: Sistine Chapel ceiling
- 53: The School of Athens
- 55: Sistine Madonna
- 56: Florence
- 58: Vatican City
- 59: High Renaissance
- 60: workshop
- 61: Ginevra de' Benci
- 63: Donato Bramante
- 64: St. Peter's Basilica
- 66: Leo XIII

#### Expected Edges (29)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 37: Michelangelo -[rival of]-> Leonardo da Vinci
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 39: Raphael -[student of]-> Pietro Perugino
- 40: Michelangelo -[rival of]-> Raphael
- 41: Lorenzo de' Medici -[sponsor]-> Leonardo da Vinci
- 42: Lorenzo de' Medici -[sponsor]-> Michelangelo
- 43: Lorenzo de' Medici -[sponsor]-> Raphael
- 44: Julius II -[sponsor]-> Michelangelo
- 45: Julius II -[sponsor]-> Raphael
- 46: Lorenzo de' Medici -[member of]-> House of Medici
- 47: House of Medici -[location]-> Florence
- 49: Leonardo da Vinci -[part of]-> High Renaissance
- 50: Michelangelo -[part of]-> High Renaissance
- 51: Raphael -[part of]-> High Renaissance
- 52: Leonardo da Vinci -[creator]-> Mona Lisa
- 53: Leonardo da Vinci -[creator]-> The Last Supper
- 54: Sistine Chapel ceiling -[commissioned by]-> Julius II
- 55: Sistine Chapel ceiling -[location]-> Vatican City
- 56: The School of Athens -[paid for]-> Julius II
- 57: The School of Athens -[location]-> Vatican City
- 59: Sistine Madonna -[creator]-> Raphael
- 60: Andrea del Verrocchio -[has a]-> workshop
- 61: Leonardo da Vinci -[tried new techniques with]-> Ginevra de' Benci
- 63: Raphael -[student of]-> Donato Bramante
- 64: Donato Bramante -[architect]-> St. Peter's Basilica
- 65: St. Peter's Basilica -[commissioned by]-> Julius II
- 66: Raphael -[patron of]-> Leo XIII
- 67: Leo XIII -[employer]-> Vatican City

#### Nodes With Yellow Tick Badge
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 48: Lorenzo de' Medici
- 63: Donato Bramante


| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| Click the "Select Properties" dropdown | The properties dropdown opens | - |
| In the "Search properties..." input, type "instance of" | The list shows property "instance of" | - |
| Check the checkbox for "instance of" | Property "instance of" is selected | - |
| Click outside the dropdown to close it | The properties dropdown closes | - |
| Click the "Property Values" dropdown | The property values dropdown opens | - |
| Leave the top selector as "All Properties" | "All Properties" is selected | - |
| In the "Search values..." input, type "human" | The list shows value "human" | - |
| Check the checkbox for value "human" | Value "human" is selected | - |
| Click outside the dropdown to close it | The property values dropdown closes | - |
| In "Relation Depth", select "1 Level" | "Relation Depth" is set to "1 Level" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (20)" and "Edges (28)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |


#### Expected Nodes (20)
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 47: Julius II
- 48: Lorenzo de' Medici
- 49: House of Medici
- 50: Mona Lisa
- 51: The Last Supper
- 52: Sistine Chapel ceiling
- 53: The School of Athens
- 55: Sistine Madonna
- 58: Vatican City
- 59: High Renaissance
- 60: workshop
- 61: Ginevra de' Benci
- 63: Donato Bramante
- 64: St. Peter's Basilica
- 66: Leo XIII

#### Expected Edges (28)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 37: Michelangelo -[rival of]-> Leonardo da Vinci
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 39: Raphael -[student of]-> Pietro Perugino
- 40: Michelangelo -[rival of]-> Raphael
- 41: Lorenzo de' Medici -[sponsor]-> Leonardo da Vinci
- 42: Lorenzo de' Medici -[sponsor]-> Michelangelo
- 43: Lorenzo de' Medici -[sponsor]-> Raphael
- 44: Julius II -[sponsor]-> Michelangelo
- 45: Julius II -[sponsor]-> Raphael
- 46: Lorenzo de' Medici -[member of]-> House of Medici
- 49: Leonardo da Vinci -[part of]-> High Renaissance
- 50: Michelangelo -[part of]-> High Renaissance
- 51: Raphael -[part of]-> High Renaissance
- 52: Leonardo da Vinci -[creator]-> Mona Lisa
- 53: Leonardo da Vinci -[creator]-> The Last Supper
- 54: Sistine Chapel ceiling -[commissioned by]-> Julius II
- 55: Sistine Chapel ceiling -[location]-> Vatican City
- 56: The School of Athens -[paid for]-> Julius II
- 57: The School of Athens -[location]-> Vatican City
- 59: Sistine Madonna -[creator]-> Raphael
- 60: Andrea del Verrocchio -[has a]-> workshop
- 61: Leonardo da Vinci -[tried new techniques with]-> Ginevra de' Benci
- 63: Raphael -[student of]-> Donato Bramante
- 64: Donato Bramante -[architect]-> St. Peter's Basilica
- 65: St. Peter's Basilica -[commissioned by]-> Julius II
- 66: Raphael -[patron of]-> Leo XIII
- 67: Leo XIII -[employer]-> Vatican City

#### Nodes With Yellow Tick Badge
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 47: Julius II
- 48: Lorenzo de' Medici
- 63: Donato Bramante
- 66: Leo XIII


| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| Click the "Select Properties" dropdown | The properties dropdown opens | - |
| In the "Search properties..." input, type "instance of" | The list shows property "instance of" | - |
| Check the checkbox for "instance of" | Property "instance of" is selected | - |
| Click outside the dropdown to close it | The properties dropdown closes | - |
| Click the "Property Values" dropdown | The property values dropdown opens | - |
| Leave the top selector as "All Properties" | "All Properties" is selected | - |
| In the "Search values..." input, type "human" | The list shows value "human" | - |
| Check the checkbox for value "human" | Value "human" is selected | - |
| Click outside the dropdown to close it | The property values dropdown closes | - |
| In "Relation Depth", select "2 Levels" | "Relation Depth" is set to "2 Levels" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (23)" and "Edges (31)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |


#### Expected Nodes (23)
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 47: Julius II
- 48: Lorenzo de' Medici
- 49: House of Medici
- 50: Mona Lisa
- 51: The Last Supper
- 52: Sistine Chapel ceiling
- 53: The School of Athens
- 54: The Creation of Adam
- 55: Sistine Madonna
- 56: Florence
- 57: Rome
- 58: Vatican City
- 59: High Renaissance
- 60: workshop
- 61: Ginevra de' Benci
- 63: Donato Bramante
- 64: St. Peter's Basilica
- 66: Leo XIII

#### Expected Edges (31)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 37: Michelangelo -[rival of]-> Leonardo da Vinci
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 39: Raphael -[student of]-> Pietro Perugino
- 40: Michelangelo -[rival of]-> Raphael
- 41: Lorenzo de' Medici -[sponsor]-> Leonardo da Vinci
- 42: Lorenzo de' Medici -[sponsor]-> Michelangelo
- 43: Lorenzo de' Medici -[sponsor]-> Raphael
- 44: Julius II -[sponsor]-> Michelangelo
- 45: Julius II -[sponsor]-> Raphael
- 46: Lorenzo de' Medici -[member of]-> House of Medici
- 47: House of Medici -[location]-> Florence
- 48: Vatican City -[location]-> Rome
- 49: Leonardo da Vinci -[part of]-> High Renaissance
- 50: Michelangelo -[part of]-> High Renaissance
- 51: Raphael -[part of]-> High Renaissance
- 52: Leonardo da Vinci -[creator]-> Mona Lisa
- 53: Leonardo da Vinci -[creator]-> The Last Supper
- 54: Sistine Chapel ceiling -[commissioned by]-> Julius II
- 55: Sistine Chapel ceiling -[location]-> Vatican City
- 56: The School of Athens -[paid for]-> Julius II
- 57: The School of Athens -[location]-> Vatican City
- 58: The Creation of Adam -[part of]-> Sistine Chapel ceiling
- 59: Sistine Madonna -[creator]-> Raphael
- 60: Andrea del Verrocchio -[has a]-> workshop
- 61: Leonardo da Vinci -[tried new techniques with]-> Ginevra de' Benci
- 63: Raphael -[student of]-> Donato Bramante
- 64: Donato Bramante -[architect]-> St. Peter's Basilica
- 65: St. Peter's Basilica -[commissioned by]-> Julius II
- 66: Raphael -[patron of]-> Leo XIII
- 67: Leo XIII -[employer]-> Vatican City

#### Nodes With Yellow Tick Badge
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 47: Julius II
- 48: Lorenzo de' Medici
- 63: Donato Bramante
- 66: Leo XIII


| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| In "Advanced Graph Search", click the "Select Nodes" dropdown | The node dropdown opens | - |
| In the "Search nodes..." input, type "Leonardo" | The list shows "Leonardo da Vinci" | - |
| Check the checkbox for "Leonardo da Vinci" | "Leonardo da Vinci" is selected | - |
| Click outside the dropdown to close it | The node dropdown closes | - |
| Click the "Select Edge Types" dropdown | The edge types dropdown opens | - |
| In the "Search edge types..." input, type "student" | The list shows "student of" | - |
| Check the checkbox for "student of" | "student of" is selected | - |
| Click outside the dropdown to close it | The edge types dropdown closes | - |
| In "Relation Depth", select "1 Level" | "Relation Depth" is set to "1 Level" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (11)" and "Edges (15)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |


#### Expected Nodes (11)
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 48: Lorenzo de' Medici
- 50: Mona Lisa
- 51: The Last Supper
- 59: High Renaissance
- 61: Ginevra de' Benci
- 63: Donato Bramante

#### Expected Edges (15)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 37: Michelangelo -[rival of]-> Leonardo da Vinci
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 39: Raphael -[student of]-> Pietro Perugino
- 40: Michelangelo -[rival of]-> Raphael
- 41: Lorenzo de' Medici -[sponsor]-> Leonardo da Vinci
- 42: Lorenzo de' Medici -[sponsor]-> Michelangelo
- 43: Lorenzo de' Medici -[sponsor]-> Raphael
- 49: Leonardo da Vinci -[part of]-> High Renaissance
- 50: Michelangelo -[part of]-> High Renaissance
- 51: Raphael -[part of]-> High Renaissance
- 52: Leonardo da Vinci -[creator]-> Mona Lisa
- 53: Leonardo da Vinci -[creator]-> The Last Supper
- 61: Leonardo da Vinci -[tried new techniques with]-> Ginevra de' Benci
- 63: Raphael -[student of]-> Donato Bramante

#### Nodes With Yellow Tick Badge
- 42: Leonardo da Vinci

#### Matched (Animated) Edges
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 39: Raphael -[student of]-> Pietro Perugino
- 63: Raphael -[student of]-> Donato Bramante


| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| In "Advanced Graph Search", click the "Select Nodes" dropdown | The node dropdown opens | - |
| In the "Search nodes..." input, type "Leonardo" | The list shows "Leonardo da Vinci" | - |
| Check the checkbox for "Leonardo da Vinci" | "Leonardo da Vinci" is selected | - |
| Click outside the dropdown to close it | The node dropdown closes | - |
| Click the "Select Edge Types" dropdown | The edge types dropdown opens | - |
| In the "Search edge types..." input, type "student" | The list shows "student of" | - |
| Check the checkbox for "student of" | "student of" is selected | - |
| Click outside the dropdown to close it | The edge types dropdown closes | - |
| In "Relation Depth", select "2 Levels" | "Relation Depth" is set to "2 Levels" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (17)" and "Edges (23)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |


#### Expected Nodes (17)
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 47: Julius II
- 48: Lorenzo de' Medici
- 49: House of Medici
- 50: Mona Lisa
- 51: The Last Supper
- 55: Sistine Madonna
- 59: High Renaissance
- 60: workshop
- 61: Ginevra de' Benci
- 63: Donato Bramante
- 64: St. Peter's Basilica
- 66: Leo XIII

#### Expected Edges (23)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 37: Michelangelo -[rival of]-> Leonardo da Vinci
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 39: Raphael -[student of]-> Pietro Perugino
- 40: Michelangelo -[rival of]-> Raphael
- 41: Lorenzo de' Medici -[sponsor]-> Leonardo da Vinci
- 42: Lorenzo de' Medici -[sponsor]-> Michelangelo
- 43: Lorenzo de' Medici -[sponsor]-> Raphael
- 44: Julius II -[sponsor]-> Michelangelo
- 45: Julius II -[sponsor]-> Raphael
- 46: Lorenzo de' Medici -[member of]-> House of Medici
- 49: Leonardo da Vinci -[part of]-> High Renaissance
- 50: Michelangelo -[part of]-> High Renaissance
- 51: Raphael -[part of]-> High Renaissance
- 52: Leonardo da Vinci -[creator]-> Mona Lisa
- 53: Leonardo da Vinci -[creator]-> The Last Supper
- 59: Sistine Madonna -[creator]-> Raphael
- 60: Andrea del Verrocchio -[has a]-> workshop
- 61: Leonardo da Vinci -[tried new techniques with]-> Ginevra de' Benci
- 63: Raphael -[student of]-> Donato Bramante
- 64: Donato Bramante -[architect]-> St. Peter's Basilica
- 65: St. Peter's Basilica -[commissioned by]-> Julius II
- 66: Raphael -[patron of]-> Leo XIII

#### Nodes With Yellow Tick Badge
- 42: Leonardo da Vinci

#### Matched (Animated) Edges
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 39: Raphael -[student of]-> Pietro Perugino
- 63: Raphael -[student of]-> Donato Bramante


| STEP | EXPECTED BEHAVIOR | RESULT |
| --- | --- | --- |
| Navigate to http://13.60.88.202:3000/spaces/8 | Space detail page "The High Renaissance and Rivalries Within" is displayed | - |
| Scroll to the "Advanced Graph Search" section | The "Advanced Graph Search" section is visible | - |
| If the "Clear" button is visible, click "Clear" | All Advanced Graph Search selections are cleared | - |
| In "Advanced Graph Search", click the "Select Nodes" dropdown | The node dropdown opens | - |
| In the "Search nodes..." input, type "Leonardo" | The list shows "Leonardo da Vinci" | - |
| Check the checkbox for "Leonardo da Vinci" | "Leonardo da Vinci" is selected | - |
| Click outside the dropdown to close it | The node dropdown closes | - |
| Click the "Select Properties" dropdown | The properties dropdown opens | - |
| In the "Search properties..." input, type "P106" | The list shows property "P106" | - |
| Check the checkbox for "P106" | Property "P106" is selected | - |
| Click outside the dropdown to close it | The properties dropdown closes | - |
| In "Relation Depth", select "1 Level" | "Relation Depth" is set to "1 Level" | - |
| Click the "Search Graph" button | The button text changes to "Searching..." while the request is in progress | - |
| Wait for the search to complete | The "Graph Search Results" panel is shown with "Nodes (17)" and "Edges (23)" | - |
| Verify the graph contains exactly these nodes | See "Expected Nodes" below | - |
| Verify the graph contains exactly these edges | See "Expected Edges" below | - |
| Verify which nodes show the yellow tick badge | See "Nodes With Yellow Tick Badge" below | - |


#### Expected Nodes (17)
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 47: Julius II
- 48: Lorenzo de' Medici
- 49: House of Medici
- 50: Mona Lisa
- 51: The Last Supper
- 55: Sistine Madonna
- 59: High Renaissance
- 60: workshop
- 61: Ginevra de' Benci
- 63: Donato Bramante
- 64: St. Peter's Basilica
- 66: Leo XIII

#### Expected Edges (23)
- 36: Leonardo da Vinci -[student of]-> Andrea del Verrocchio
- 37: Michelangelo -[rival of]-> Leonardo da Vinci
- 38: Raphael -[influenced by]-> Leonardo da Vinci
- 39: Raphael -[student of]-> Pietro Perugino
- 40: Michelangelo -[rival of]-> Raphael
- 41: Lorenzo de' Medici -[sponsor]-> Leonardo da Vinci
- 42: Lorenzo de' Medici -[sponsor]-> Michelangelo
- 43: Lorenzo de' Medici -[sponsor]-> Raphael
- 44: Julius II -[sponsor]-> Michelangelo
- 45: Julius II -[sponsor]-> Raphael
- 46: Lorenzo de' Medici -[member of]-> House of Medici
- 49: Leonardo da Vinci -[part of]-> High Renaissance
- 50: Michelangelo -[part of]-> High Renaissance
- 51: Raphael -[part of]-> High Renaissance
- 52: Leonardo da Vinci -[creator]-> Mona Lisa
- 53: Leonardo da Vinci -[creator]-> The Last Supper
- 59: Sistine Madonna -[creator]-> Raphael
- 60: Andrea del Verrocchio -[has a]-> workshop
- 61: Leonardo da Vinci -[tried new techniques with]-> Ginevra de' Benci
- 63: Raphael -[student of]-> Donato Bramante
- 64: Donato Bramante -[architect]-> St. Peter's Basilica
- 65: St. Peter's Basilica -[commissioned by]-> Julius II
- 66: Raphael -[patron of]-> Leo XIII

#### Nodes With Yellow Tick Badge
- 42: Leonardo da Vinci
- 43: Michelangelo
- 44: Raphael
- 45: Andrea del Verrocchio
- 46: Pietro Perugino
- 48: Lorenzo de' Medici
- 63: Donato Bramante


### Voting
Test Case 1: Upvote a Discussion Successfully and Verify Persistence
| Step | Expected Behavior                                             | Result   |
|------|---------------------------------------------------------------|----------|
| Open the application | Login screen displayed | - |
| Login to the application | Home page displayed | - |
| Navigate to a space with discussions | Space detail page displayed with Discussions section | - |
| Locate a discussion comment          | Discussion text, author, and vote buttons visible    | - |
| Click the upvote button (👍) next to a discussion | Upvote button highlighted/activated | - |
| Check the upvote count               | Upvote count increases by 1                          | - |
| Verify vote persistence              | Refresh page; upvote button remains highlighted      | Passed |

Test Case 2: Upvote a Discussion When Login Fails
| Step | Expected Behavior                                             | Result      |
|------|---------------------------------------------------------------|-------------|
| Open the application | Login screen displayed                        | -   |
| Login to the application | Home page displayed                       |  Failed   |
| Navigate to a space with discussions | Space detail page displayed with Discussions section | Blocked |
| Locate a discussion comment | Discussion text, author, and vote buttons visible |  Blocked |
| Click the upvote button (👍) next to a discussion | Upvote button highlighted/activated |  Blocked |
| Check the upvote count | Upvote count increases by 1                  | Blocked |
| Verify vote persistence | Refresh page; upvote button remains highlighted | Blocked |


| Step                                         | Expected Behavior                                              | Result       |
|----------------------------------------------|----------------------------------------------------------------|--------------|
| Open the application                         | Login screen displayed                                         | -    |
| Login to the application                     | Home page displayed                                            | Failed    |
| Navigate to a space with discussions         | Space detail page displayed with Discussions section           | Blocked  |
| Locate a discussion comment that is not voted on | Discussion with down vote buttons visible               | Blocked  |
| Click the downvote button (👎) next to a discussion | Downvote button highlighted/activated                     | Blocked  |
| Check the downvote count                     | Downvote count increases by 1                                  | Blocked  |
| Verify vote persistence                      | Refresh page; downvote button remains highlighted              | Blocked  |

Test Case 4: Downvote a Discussion Successfully and Verify Persistence
| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login to the application | Home page displayed | - |
| Navigate to a space with discussions | Space detail page displayed with Discussions section | - |
| Locate a discussion comment that is not voted on | Discussion with down vote buttons visible | - |
| Click the downvote button (👎) next to a discussion | Downvote button highlighted/activated | - |
| Check the downvote count | Downvote count increases by 1 | - |
| Verify vote persistence | Refresh page; downvote button remains highlighted | Passed |

### Discussion

Test Case 1: Sending Comment While Being a Collabrator in a Space (Success)

| Step | Expected Behavior | Result |
|-----|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login to the application | Home page displayed | - |
| Navigate to a space where you are a collaborator | Space detail page displayed with Discussions section | - |
| Scroll to the Discussions section | Comment input box visible at the top of the discussion section | - |
| Type a comment in the text field | Text appears in the input box | - |
| Click the "Post Comment" button | Comment is posted and appears in the discussion list | - |
| Verify comment details | Comment shows your username, text, and timestamp | - |
| Check input field | Comment input field is cleared automatically | Passed |

Test Case 2: Sending Comment Without Being a Collaborator (Success)

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login to the application | Home page displayed | - |
| Navigate to a space where you are NOT a collaborator | Space detail page displayed with Discussions section | - |
| Scroll to the Discussions section | Comment input box should be disabled | Success |


### Test Case 3: Sending Empty Comment (Success)

| Step | Expected Behavior                                                     | Result      |
|------|-----------------------------------------------------------------------|-------------|
| Open the application | Login screen displayed                                     | -   |
| Login to the application | Home page displayed                                     | -   |
| Navigate to a space where you are a collaborator | Space detail page displayed with Discussions section | -  |
| Scroll to the Discussions section | Comment input box visible at the top of the discussion           | - |
| Leave the comment text field empty | Input field remains empty                            | - |
| Click the "Post Comment" button | Empty message will not be displayed in the discussion section  | -   |
| Verify no comment is posted | Discussion list remains unchanged                     | Success  |

### Activity Stream

Test Case 1: Viewing Activity Stream

| Step                               | Expected Behavior                                                                 | Result |
|------------------------------------|-----------------------------------------------------------------------------------|--------|
| Open the application               | Login screen displayed                                                            | -      |
| Login to the application           | Home page displayed                                                               | -      |
| Navigate to Home page or Dashboard | Activity Stream section visible                                                   | -      |
| Check activity feed display        | Recent activities displayed in chronological order (newest first)                 | -      |
| Verify activity information        | Each activity shows actor, action type, object, summary, and timestamp            | Passed  |


Test Case 3: Activity Stream Without Authentication
| Step                                                       | Expected Behavior                                               | Result |
|------------------------------------------------------------|-----------------------------------------------------------------|--------|
| Open the application                                       | Login screen displayed                                          | -      |
| Attempt to access activity stream URL directly without login | Redirected to login page or access denied                       | -      |
| Login to the application                                   | Home page with activity stream displayed                        | -      |
| Verify authentication requirement                          | Activity stream only accessible when authenticated              | Passed |


Test Case 3: Activity Payload Information

| Step                                             | Expected Behavior                                                     | Result |
|--------------------------------------------------|------------------------------------------------------------------------|--------|
| Perform various actions (create space, add node, etc.) | Activities are logged                                                  | -      |
| Check activity details via API                   | Each activity includes payload with context (space_id, node_id, etc.) | -      |
| Verify space activity payload                    | Contains space_id and space_title                                     | -      |
| Verify node activity payload                     | Contains space_id, node_id, and node_label                             | -      |
| Verify edge activity payload                     | Contains edge_id, source_id, and target_id                             | Passed |


Test Case 4: Real-Time Activity Updates

| Step                                   | Expected Behavior                                             | Result |
|----------------------------------------|---------------------------------------------------------------|--------|
| Open activity stream                   | Current activities displayed                                  | -      |
| Perform a new action (create a space) | New activity does not appear automatically (no real-time) | -      |
| Refresh the page                       | New activity now appears at the top of the feed               | -      |
| Verify manual refresh requirement      | Real-time updates require page refresh                        | Passed |

Test Case 5: Activity Stream Empty State
| Step                              | Expected Behavior                                      | Result |
|-----------------------------------|--------------------------------------------------------|--------|
| Login with a brand new user account| Home page displayed                                    | -      |
| Navigate to Activity Stream       | Empty state message displayed: "No activitiy yet"     | -      |
| Join a space                      | Activity appears in the feed                           | -      |
| Verify initial empty state        | Appropriate message shown when no activities exist     | Passed |

Test Case 6: Real-Time Activity Updates
| Step                                   | Expected Behavior                                             | Result     |
|----------------------------------------|---------------------------------------------------------------|------------|
| Open activity stream                   | Current activities displayed                                  | -  |
| Perform a new action (create a space) | New activity does not appear automatically (no real-time) | -  |
| Refresh the page                       | New activity now appears at the top of the feed               | Failed  |
| Verify manual refresh requirement      | Real-time updates require page refresh                        | Blocked |


### Report&Archive&Dismiss

## Report

### Test Case 1 : Reporting a Space
| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login to the application | Home page displayed | - |
| Navigate to a space | Space detail page displayed | - |
| Click the "Space Actions" button on the right corner | Dropdown menu appears | - |
| Click "Report" from the dropdown | Report modal opens with space information | - |
| Verify modal shows content type | Modal displays "Content Type: Space" | - |
| Select a reason from dropdown ("Spam") | Reason selected in dropdown | - |
| Click "Submit Report" button | Report submitted successfully | - |
| Check for confirmation message | Success message displayed | - |
| Verify modal closes | Modal closes automatically | Passed |


### Test Case 2 : Reporting a Discussion

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login to the application | Home page displayed | - |
| Navigate to a space with discussions | Space detail page displayed | - |
| Locate a discussion comment | Discussion visible | - |
| Click "Report" button in the comment | Report modal opens | - |
| Verify modal content | Discussion preview displayed | - |
| Select a reason ( "Off-topic") | Reason selected | - |
| Click "Submit Report" | Report submitted successfully | Passed |

### Test Case 3: Reporting a Node

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login to the application | Home page displayed | - |
| Navigate to a space with nodes | Graph visualization displayed | - |
| Click on a node | Node details or menu appears | - |
| Select "Report" | Report modal opens | - |
| Verify node label | Node label displayed | - |
| Select a reason | Reason selected | - |
| Click "Submit Report" | Report submitted | Passed |

### Test Case 4: Reporting a Profile

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login to the application | Home page displayed | - |
| Navigate to user profile from activity stream | Profile page displayed | - |
| Locate "Report" button | Report button visible | - |
| Click "Report" | Report modal opens | - |
| Select a reason | Reason selected | - |
| Click "Submit Report" | Report submitted | Passed |

### Test Case 6: Report Without Selecting Reason

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login to the application | Home page displayed | - |
| Navigate to user profile from activity stream | Profile page displayed | - |
| Locate "Report" button | Report button visible | - |
| Open report modal | Modal displayed | - |
| Leave reason unselected | No reason selected | - |
| Click "Submit Report" | Error message shown | - |
| Verify submit disabled | Button disabled until reason selected | - |
| Select reason and submit | Report submitted | Passed |

### Test Case 7: Viewing Reports as Admin

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Login as Admin | Home page displayed | - |
| Click to Admin button from the right corner of the application | Drop down list appeared | - |
| Select "Admin Dashboard" from the drop down list | Admin dashboard displayed | - |
| Open Reports section from the left menu | Reports list displayed | - |
| Verify grouping | Reports grouped by content | - |
| Check report details | Full report info visible | - |
| Verify actions | Dismiss & Archive available | Passed |

### Test Case 8: Dismissing a Report as Admin

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Login as Admin | Home page displayed | - |
| Click to Admin button from the right corner of the application | Drop down list appeared | - |
| Select "Admin Dashboard" from the drop down list | Admin dashboard displayed | - |
| Open Reports section from the left menu | Reports list displayed | - |
| Locate report group | Report visible | - |
| Click "Dismiss" | Reports dismissed | - |
| Verify status | Status changed to Dismissed | - |
| Verify flags | report_count reset, is_reported false | Passed |

### Test Case 9: Archiving a Report as Admin

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Login as Admin | Home page displayed | - |
| Click to Admin button from the right corner of the application | Drop down list appeared | - |
| Select "Admin Dashboard" from the drop down list | Admin dashboard displayed | - |
| Open Reports section from the left menu | Reports list displayed | - |
| Click "Archive" button for a reported group| Confirmation requested | - |
| Confirm archive | Reports archived | - |
| Click the "Archive" button from the left menu| Archived items listed | - |
| Verifiy archived item is visible in the list| Archived item is in the archive list | Passed |

### Map

Test Case 1: Calculate Distance Between Nodes in Space Map

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Login as regular user | Home page displayed | - |
| Navigate to Space with nodes (Space ID: 100) | Space detail page displayed | - |
| Click "Space Actions" button | Drop down list appears | - |
| Click "Show Space Map" button on the drop down list | Space map modal opens | - |
| Wait for map to load | Map displayed with space location centered | - |
| Check nodes with location data | Green node markers displayed on map | - |
| Click first node marker (Node A) | Node marker turns red, selected state shown | - |
| Verify selection indicator | Popup shows "✓ Selected for distance calculation" | - |
| Click second node marker (Node B) | Node marker turns red, both nodes selected | - |
| Check distance line appears | Red polyline connects Node A and Node B | Passed |
| Verify distance label | Midpoint label shows distance in km (e.g., "15.3 km") | Passed |
| Select third node (Node C) | Distance lines drawn between all pairs: A-B, B-C, A-C | Passed |
| Verify multiple distances | All three distance labels visible at connection midpoints | Passed |
| Click selected node to deselect | Node returns to green, associated distance lines removed | Passed |

Test Case 2: Map Marker Clustering with Multiple Spaces

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Login as regular user | Home page displayed | - |
| Create 3 spaces in same city: "Berlin, Germany" | Three spaces created with same city | - |
| Navigate to Map by clicking the Map button on the header | Map view loads and geocodes all spaces | - |
| Check marker display for Berlin spaces | Red cluster marker with count "3" displayed | - |
| Verify cluster animation | Pulse animation visible on main cluster marker | - |
| Check cluster member offsets | Individual green markers slightly offset around main cluster | - |
| Click main cluster marker (red) | Popup shows first space details | - |
| Click offset green marker | Popup shows corresponding cluster member space | - |
| Zoom in on cluster | Markers separate as zoom level increases | - |
| Create space 10km away in Berlin suburbs | New marker should be separate, not clustered | - |
| Verify clustering distance logic | Only spaces <5km or same exact city are clustered | Passed |


Test Case 4 : Map View with No Location Data

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Login as regular user | Home page displayed | - |
| Create space without location data | Space created with null city/country | - |
| Navigate to Map View | Map view loads successfully | - |
| Check geocoding process | Geocoding skipped for spaces without location | - |
| Verify space not displayed on map | No marker appears for space without coordinates | - |
| Check error handling | No errors logged for missing location data | - |
| View Space Analytics page | Location section shows "Location not specified" | - |
| Edit space to add location | Add city "Paris", country "France" | - |
| Return to Map View | Space now visible after geocoding | - |
| Verify geocoding triggered | Nominatim API called for new location | - |
| Confirm marker displayed | Space marker appears at Paris coordinates | Passed |

Test Case 5: Display All Spaces on Map View

| Step | Expected Behavior | Result |
|------|-------------------|--------|
| Login as regular user | Home page displayed | - |
| Navigate to Map View from Map button on left corner of the page | Map view page loads | - |
| Wait for initial map render | World map displayed with default view | - |
| Check loading indicator | "Fetching collabration spaces.." progress message shown | - |
| Wait for geocoding completion | All spaces with location data geocoded | - |
| Check map markers displayed | Blue pin markers appear for single spaces | - |
| Verify clustered spaces | Red cluster markers with count shown for nearby spaces | - |
| Check cluster within 5km radius | Spaces within 5km grouped together | - |
| Verify same city clustering | Spaces in same city grouped even if >5km apart | - |
| Click on single space marker | Popup opens with space details | - |
| Verify spaces in the left list | Title, description, location shown for spaces | - |
| Click one of the spaces from the left menu | Navigate to space detail page | Passed |




### Mobile Manual Tests

## LOGIN

### 1. Log in to the Registration Screen Test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Click on the "Sign Up" link at the bottom of the login screen | Registration screen displayed | Passed |

### 2. Login with empty credentials test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Leave the username or password field empty | - | - |
| Click the "Login" button | The error message is shown as "Please fill all fields." | Passed |

### 3. Pressing ok button in error should close the error dialog in the login test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Leave the username or password field empty | - | - |
| Click the "Login" button | The error message is shown as "Please fill all fields." | - |
| Click the "OK" button on the error dialog (or dismiss the error) | Dialog is closed, user stays on the same page | Passed |

### 4. Login with valid credentials test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Enter username: "esranrzm" in the username field | - | - |
| Enter password: "esra1234" in the password field | - | - |
| Click the "Login" button | User navigated to the home page | Passed |

### 5. Login with invalid credentials test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Enter username: "esranrzm" in the username field | - | - |
| Enter password: "esra123456" in the password field | - | - |
| Click the "Login" button | Error message is shown as "Invalid credentials" | Passed |

## REGISTER

### 1. Register with empty fields test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Click on "Sign Up" link at the bottom of the login screen | - | - |
| Leave email field empty | - | - |
| Leave username field empty | - | - |
| Leave password field empty | - | - |
| Leave profession field empty | - | - |
| Leave date of birth field empty | - | - |
| Do not select a country | - | - |
| Click the "Register" button | The error message is shown as "Please fill all fields." | Passed |

### 2. Register with invalid email test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Click on "Sign Up" link at the bottom of the login screen | - | - |
| Enter email: "invalidEmail.com" in the email field (missing @ symbol) | - | - |
| Enter username: "johnDoe" in the username field | - | - |
| Enter password: "john1234" in the password field | - | - |
| Enter profession: "Engineer" in the profession field | - | - |
| Click on date of birth field | date picker opens | - |
| Select date: "01/01/2000" | - | - |
| Click on country dropdown | country dropdown opens | - |
| Search and select "Turkey" from the country list | country is selected, city request is sent | - |
| Wait for cities to load | - | - |
| Click on city dropdown | city dropdown opens | - |
| Search and select "Istanbul" from the city list | city is selected | - |
| Click the "Register" button | The error message is shown as "Invalid email" | Passed |

### 3. Register with invalid profession test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Click on "Sign Up" link at the bottom of the login screen | - | - |
| Enter email: "john@example.com" | - | - |
| Enter username: "johnDoe" in the username field | - | - |
| Enter password: "john1234" in the password field | - | - |
| Enter profession: "123Engineer!" in the profession field (contains numbers and special characters) | - | - |
| Click on date of birth field | date picker opens | - |
| Select date: "01/01/2000" | - | - |
| Click on country dropdown | country dropdown opens | - |
| Search and select "Turkey" from the country list | country is selected, city request is sent | - |
| Wait for cities to load | - | - |
| Click on city dropdown | city dropdown opens | - |
| Search and select "Istanbul" from the city list | city is selected | - |
| Click the "Register" button | The error message is shown as "Invalid profession" | Passed |

### 4. Register with invalid age test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Click on "Sign Up" link at the bottom of the login screen | - | - |
| Enter email: "john@example.com" | - | - |
| Enter username: "johnDoe" in the username field | - | - |
| Enter password: "john1234" in the password field | - | - |
| Enter profession: "Engineer" in the profession field | - | - |
| Click on date of birth field | date picker opens | - |
| Select date: "01/01/2015" | - | - |
| Click on country dropdown | country dropdown opens | - |
| Search and select "Turkey" from the country list | country is selected, city request is sent | - |
| Wait for cities to load | - | - |
| Click on city dropdown | city dropdown opens | - |
| Search and select "Istanbul" from the city list | city is selected | - |
| Click the "Register" button | The error message is shown as "Age must be 18+" | Passed |

### 5. Register without country test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Click on "Sign Up" link at the bottom of the login screen | - | - |
| Enter email: "john@example.com" | - | - |
| Enter username: "johnDoe" in the username field | - | - |
| Enter password: "john1234" in the password field | - | - |
| Enter profession: "Engineer" in the profession field | - | - |
| Click on date of birth field | date picker opens | - |
| Select date: "01/01/2000" | - | - |
| Do not select a country (leave country field empty) | - | - |
| Click the "Register" button | The error message is shown as "Please select a country" | Passed |

### 6. Register with valid inputs and a successful response from the registration api test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Click on "Sign Up" link at the bottom of the login screen | - | - |
| Enter email: "john@example.com" | - | - |
| Enter username: "johnDoe" in the username field | - | - |
| Enter password: "john1234" in the password field | - | - |
| Enter profession: "Engineer" in the profession field | - | - |
| Click on date of birth field | date picker opens | - |
| Select date: "01/01/2000" | - | - |
| Click on country dropdown | country dropdown opens | - |
| Search and select "Turkey" from the country list | country is selected, city request is sent | - |
| Wait for cities to load | - | - |
| Click on city dropdown | city dropdown opens | - |
| Search and select "Istanbul" from the city list | city is selected | - |
| Click the "Register" button | User navigated to login | Passed |

### 7. Register with valid inputs and an unsuccessful response from the registration api test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Click on "Sign Up" link at the bottom of the login screen | - | - |
| Enter email: "john@example.com" | - | - |
| Enter username: "johnDoe" in the username field | - | - |
| Enter password: "john1234" in the password field | - | - |
| Enter profession: "Engineer" in the profession field | - | - |
| Click on date of birth field | date picker opens | - |
| Select date: "01/01/2000" | - | - |
| Click on country dropdown | country dropdown opens | - |
| Search and select "Turkey" from the country list | country is selected, city request is sent | - |
| Wait for cities to load | - | - |
| Click on city dropdown | city dropdown opens | - |
| Search and select "Istanbul" from the city list | city is selected | - |
| Click the "Register" button | The error message is shown as "Registration failed" | Passed |

### 8. Pressing OK button should close the error dialog in the registration screen test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Click on "Sign Up" link at the bottom of the login screen | User navigated to the registration screen | - |
| Leave all fields empty | - | - |
| Click the "Register" button | - | - |
| Click "OK" button on the error dialog | dialog is closed | Passed |

### 9. The login text button navigates to the login in registration test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Click on "Sign Up" link at the bottom of the login screen | User navigated to the registration screen | - |
| Click on the "Sign In" link at the bottom of the registration screen | User navigated to the login screen | Passed |

## PROFILE PAGE

### 1. Get Profile Details test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login with valid credentials | user generated to home page | - |
| Click the profile button in the bottom navigation bar | user navigated to the profile screen | - |
| Profile data is loaded successfully | The user sees the profile details | Passed |

### 2. Get Profile Details failed api test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login with valid credentials | user generated to home page | - |
| Click the profile button in the bottom navigation bar | user navigated to the profile screen | - |
| Profile data is could not loaded and received error from the service | The error message is shown as "Error fetching profile" | Passed |

### 3. Get Other User Profile Details test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one space on the home screen | user navigated to the space screen | - |
| Click on the collaborators button on the screen | A dialog contains list of users is shown on the screen | - |
| Click one user from the list | User navigated to the clicked user's profile | - |
| Profile data is loaded successfully | The user sees the profile details | Passed |

### 4. Get other user profile details api failure test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one space on the home screen | user navigated to the space screen | - |
| Click on the collaborators button on the screen | A dialog contains list of users is shown on the screen | - |
| Click one user from the list | User navigated to the clicked user's profile | - |
| Profile data is could not loaded and received error from the service | The error message is shown as "Error fetching profile" | Passed |

### 5. Load update profile data test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login with valid credentials | user navigated to home page | - |
| Click the profile button in the bottom navigation bar | user navigated to the profile screen | - |
| Profile data is loaded successfully | The user sees the profile details | - |
| Click on the edit icon (pencil/edit button) at the top right of the profile screen | user navigated to edit profile screen and profile details fetched from backend | Passed |

### 6. Update profile test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login with valid credentials | user navigated to home page | - |
| Click the profile button in the bottom navigation bar | user navigated to the profile screen | - |
| Profile data is loaded successfully | The user sees the profile details | - |
| Click on the edit icon (pencil/edit button) at the top right of the profile screen | user navigated to the edit profile screen | - |
| Wait for profile data and countries to load | Profile details fetched from the backend | - |
| Edit the profession field | - | - |
| Edit the bio field | - | - |
| Click the "Save" button at the bottom of the screen | user navigated to the profile screen | - |
| - | Updated values are seen on the profile screen | Passed |

## SETTINGS

### 1. Color blind theme should be false as default test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login with valid credentials | user navigated to the home page | - |
| Click the settings button in the bottom navigation bar | user navigated to the settings screen | - |
| - | The radio button should stay at unchecked for color blind theme | Passed |

### 2. Color blind theme change test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login with valid credentials | user navigated to the home page | - |
| Click the settings button in the bottom navigation bar | user navigated to the settings screen | - |
| Change the preference by clicking on the radio button | The theme is changed | Passed |

### 3. Logout test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Login with valid credentials | user navigated to the home page | - |
| Click the settings button in the bottom navigation bar | user navigated to the settings screen | - |
| Click on the logout button below the page | user navigated to login screen | Passed |

## REPORT

### 1. Report user test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one space on the home screen | user navigated to the space screen | - |
| Click on the collaborators button on the screen | A dialog contains list of users is shown on the screen | - |
| Click one user from the list | User navigated to the clicked user's profile | - |
| Profile data is loaded successfully | - | - |
| Click on the "Report" button/text at the top right of the profile screen | - | - |
| The report's reasons are retrieved from the service | Report reason dropdown filled with retrieved reasons | - |
| Select one reason from the list | - | - |
| click submit report button | The report sent message is shown as a toast message on the screen | Passed |

### 2. Failed to report user test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one space on the home screen | user navigated to the space screen | - |
| Click on the collaborators button on the screen | A dialog contains list of users is shown on the screen | - |
| Click one user from the list | User navigated to the clicked user's profile | - |
| Profile data is loaded successfully | - | - |
| Click on the "Report" button/text at the top right of the profile screen | - | - |
| The report's reasons are retrieved from the service | Report reason dropdown filled with retrieved reasons | - |
| Select one reason from the list | - | - |
| click submit report button | The error message is shown as "Failed to submit report" | Passed |

### 3. Report user failure to retrieve reasons test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one space on the home screen | user navigated to the space screen | - |
| Click on the collaborators button on the screen | A dialog contains list of users is shown on the screen | - |
| Click one user from the list | User navigated to the clicked user's profile | - |
| Profile data is loaded successfully | - | - |
| Click on the "Report" button/text at the top right of the profile screen | - | - |
| The report's reasons could not be retrieved from the service | The error message is shown as "Failed to load report reasons" | Passed |

### 4. Report space test steps

| STEP                                             | EXPECTED BEHAVIOR | RESULT |
|--------------------------------------------------|-------------------|--------|
| Open the application                             | Login screen displayed | - |
| Log in with valid credentials                    | user navigated to the home page | - |
| Click one space on the home screen               | user navigated to the space screen | - |
| Click the menu icon (three dots) at the top right | dropdown menu opens | - |
| Click "Report Space" from the menu               | The report dialog appears on the screen | - |
| Click the report reason dropdown                 | drop down opens with available options | - |
| Click on the "Submit Report" button              | The report is sent to the service | - |
| - | The success message is shown on the screen as a toast message | Passed |

### 5. Failed to report space test steps

| STEP                                             | EXPECTED BEHAVIOR                           | RESULT |
|--------------------------------------------------|---------------------------------------------|--------|
| Open the application                             | Login screen displayed                      | - |
| Log in with valid credentials                    | user navigated to the home page             | - |
| Click one space on the home screen               | user navigated to the space screen          | - |
| Click the menu icon (three dots) at the top right | dropdown menu opens                         | - |
| Click "Report Space" from the menu               | The report dialog appears on the screen     | - |
| Click the report reason dropdown                 | drop down opens with available options      | - |
| Click on the "Submit Report" button              | The report could not be sent to the service | - |
| - | An error message is shown on the screen with the message “Failed to submit report.”| Passed |

### 6. Get report space reasons test steps

| STEP                                              | EXPECTED BEHAVIOR | RESULT |
|---------------------------------------------------|-------------------|------|
| Open the application                              | Login screen displayed | - |
| Log in with valid credentials                     | user navigated to the home page | - |
| Click one space on the home screen                | user navigated to the space screen | - |
| Click the menu icon (three dots) at the top right | dropdown menu opens | - |
| Click "Report Space" from the menu                | The report dialog appears on the screen | - |
| - | reasons retrieved from the service | - |
| Click the report reason dropdown               | drop down opens with available options | Passed |


### 7. Failed to get report space reasons test steps

| STEP                                             | EXPECTED BEHAVIOR | RESULT |
|--------------------------------------------------|-------------------|------|
| Open the application                             | Login screen displayed | - |
| Log in with valid credentials                    | user navigated to the home page | - |
| Click one space on the home screen               | user navigated to the space screen | - |
| Click the menu icon (three dots) at the top right | dropdown menu opens | - |
| Click "Report Space" from the menu               | The report dialog appears on the screen | - |
| - | Reasons could not be retrieved from the service | - |
| - | An empty dropdown list for the report will be seen, and reporting will not be available| Passed |

### 8. Report node test steps

| STEP                                             | EXPECTED BEHAVIOR | RESULT |
|--------------------------------------------------|----------------------------------------|---|
| Open the application                             | Login screen displayed | - |
| Log in with valid credentials                    | user navigated to the home page | - |
| Click one space on the home screen               | user navigated to the space screen | - |
| Click on a node in the list                      | Space Node Details screen is displayed | - |
| Click the menu icon (three dots) at the top right | dropdown menu opens | - |
| Click "Report Node" from the menu                | The report dialog appears on the screen | - |
| Click the report reason dropdown                 | drop down opens with available options | - |
| Click on the "Submit Report" button              | The report is sent to the service | - |
| - | The success message is shown on the screen as a toast message | Passed |

### 9. Failed to report node test steps

| STEP                                             | EXPECTED BEHAVIOR | RESULT |
|--------------------------------------------------|----------------------------------------|---|
| Open the application                             | Login screen displayed | - |
| Log in with valid credentials                    | user navigated to the home page | - |
| Click one space on the home screen               | user navigated to the space screen | - |
| Click on a node in the list                      | Space Node Details screen is displayed | - |
| Click the menu icon (three dots) at the top right | dropdown menu opens | - |
| Click "Report Node" from the menu                | The report dialog appears on the screen | - |
| Click the report reason dropdown                 | drop down opens with available options | - |
| Click on the "Submit Report" button              | The report could not be sent to the service | - |
| - | An error message is shown on the screen with the message “Failed to submit report.” | Passed |


### 10. Get report node reasons test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|---|----------------------------------------|---|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one space on the home screen | user navigated to the space screen | - |
| Click on a node in the list | Space Node Details screen is displayed | - |
| Click the menu icon (three dots) at the top right | dropdown menu opens | - |
| Click "Report Node" from the menu | The report dialog appears on the screen | - |
| - | reasons retrieved from the service | - |
| Click the report reason dropdown | drop down opens with available options | Passed |


## CREATE SPACE

### 1. Create space with no tags and location test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one Create Space button on the home screen | user navigated to the create space screen | - |
| Enter space title: "Quantum Computing Applications" | - | - |
| Enter space description: "Exploring practical applications of quantum computing..." | - | - |
| Click on create space button | User directed to space details screen | Passed |

### 2. Create space failure test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click the Create Space button on the home screen | The user navigated to the create space screen | - |
| Enter space title: "Quantum Computing Applications." | - | - |
| Enter space description: "Exploring practical applications of quantum computing..." | - | - |
| Click on create space button | space could not be created | - |
| - | The error message is shown as "Failed to create space." | Passed |

### 3. The tag search button should be disabled when the query is empty test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click the Create Space button on the home screen | The user navigated to the create space screen | - |
| Click on the "Add Tags with Wikidata" button | The search text box is shown on the screen | - |
| - | The search button is shown on the screen | - |
| leave the field empty | The search button should be disabled | Passed |

### 4. The tag search button should be disabled when the query is blank test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one Create Space button on the home screen | user navigated to the create space screen | - |
| Click on "Add Tags with wikidata" button | The search text box is shown on the screen | - |
| - | The search button is shown on the screen | - |
| Type "     " | The search button should be disabled | Passed |

### 5. The tag search button should be enabled when the query is valid test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one Create Space button on the home screen | user navigated to the create space screen | - |
| Click on "Add Tags with wikidata" button | The search text box is shown on the screen | - |
| - | The search button is shown on the screen | - |
| Type "technology" | The search button should be enabled | - |
| Click on the search button | The service call is triggered, and a loading dialog is shown to the user | - |
| - | Results are listed below the search button | Passed |

### 6. The tag search result is empty test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one Create Space button on the home screen | user navigated to the create space screen | - |
| Click on "Add Tags with wikidata" button | The search text box is shown on the screen | - |
| - | The search button is shown on the screen | - |
| Type "111111111111888" | The search button should be enabled | - |
| Click on the search button | The service call is triggered, and a loading dialog is shown to the user | - |
| - | No result found, no tag list shown on screen | Passed |

### 7. The tag search should be cleared when tag selected test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click one Create Space button on the home screen | user navigated to the create space screen | - |
| Click on "Add Tags with wikidata" button | The search text box is shown on the screen | - |
| - | The search button is shown on the screen | - |
| Type "technology" | The search button should be enabled | - |
| Click on the search button | The service call is triggered, and a loading dialog is shown to the user | - |
| - | Results are listed below the search button | - |
| Select one item from the list | search text is cleared, search button is disabled, tag is added to the Tags section | Passed |

### 8. Delete added tag test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click the Create Space button on the home screen | The user navigated to the create space screen | - |
| Click on the "Add Tags with Wikidata" button | The search text box is shown on the screen | - |
| - | The search button is shown on the screen | - |
| Type "technology" | The search button should be enabled | - |
| Click on the search button | The service call is triggered, and a loading dialog is shown to the user | - |
| - | Results are listed below the search button | - |
| Select one item from the list | search text is cleared, search button is disabled, tag is added to the Tags section | - |
| Click on the trash icon on the added tag item card | The tag is deleted | Passed |

### 9. Hiding Wikidata tag search test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click the Create Space button on the home screen | The user navigated to the create space screen | - |
| Click on the "Add Tags with Wikidata" button | The search text box is shown on the screen | - |
| - | The search button is shown on the screen | - |
| Click on the "Hide Tag Search" button | The text box is hidden, and the search button is hidden | - |
| - | The button text is changed to "Add Tags with Wikidata." | Passed |

## SPACE DETAILS

### 1. Seeing space details and discussions successfully test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space on the home screen | The user navigated to the space details screen | - |
| - | space details are fetched from the service | - |
| - | space discussions are fetched from the service | - |
| - | All details are loaded an shown on screen | Passed |

### 2. Could not seeing space details and discussions due to api error test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space on the home screen | The user navigated to the space details screen | - |
| - | Space details could not be fetched from the service | - |
| - | space discussions could not be fetched from the service | - |
| - | Related error message is shown on error dialog on screen | Passed |

### 3. Add discussion to space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space that you enrolled in on the home screen | The user navigated to the space details screen | - |
| Scroll to the "Join Discussion" section at the bottom | Discussion edit text box is seen | - |
| Enter a discussion text in the comment input field | - | - |
| Click the "Share" button | - | - |
| - | Discussion is added | - |
| - | The newly added discussion is seen on top of the discussions | Passed |

### 4. Failure in adding discussion to the space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space that you enrolled in on the home screen | The user navigated to the space details screen | - |
| Scroll to the "Join Discussion" section at the bottom | Discussion edit text box is seen | - |
| Enter a discussion text in the comment input field | - | - |
| Click the "Share" button | - | - |
| - | Discussion could not be added | - |
| - | An error dialog is shown on screen with the message "Failed to add discussion." | Passed |

### 5. Enrolling in a space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space that you are not enrolled in on the home screen | The user navigated to the space details screen | - |
| Click the "Join Space" button at the top of the screen | - | - |
| - | user joins the space and becomes a collaborator | - |
| - | The screen is refreshed | Passed |

### 6. Failed to enroll in the space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space that you are not enrolled in on the home screen | The user navigated to the space details screen | - |
| Click the "Join Space" button at the top of the screen | - | - |
| - | The user could not join the space | - |
| - | An error dialog is shown on screen with the error message "Join failed." | Passed |

### 7. Leaving a space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space that you are enrolled in on the home screen | The user navigated to the space details screen | - |
| Click the "Leave Space" button at the top of the screen | - | - |
| - | The user leaves the space and is removed from the collaborators list | - |
| - | The screen is refreshed | Passed |

### 8. Failed to leave a space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space that you are enrolled in on the home screen | The user navigated to the space details screen | - |
| Click the "Leave Space" button at the top of the screen | - | - |
| - | The user could not leave the space | - |
| - | An error dialog is shown on screen with the error message "Leave failed." | Passed |

### 9. Deleting a space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space that you are enrolled in on the home screen | The user navigated to the space details screen | - |
| Click the menu icon (three dots) at the top right | dropdown menu opens | - |
| Click "Delete Space" from the menu | Confirmation dialog opens on the screen | - |
| Click the "Delete Space" button in the confirmation dialog | Dialog is closed | - |
| - | The loading dialog is shown during api process | - |
| - | Space is deleted, and the user is navigated to the home screen | Passed |

### 10. Failed to delete a space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space that you are enrolled in on the home screen | The user navigated to the space details screen | - |
| Click the menu icon (three dots) at the top right | dropdown menu opens | - |
| Click "Delete Space" from the menu | Confirmation dialog opens on the screen | - |
| Click the "Delete Space" button in the confirmation dialog | Dialog is closed | - |
| - | The loading dialog is shown during api process | - |
| - | Space could not be deleted, and the user stays on the same page | - |
| - | An error dialog is shown on screen with a message "Delete failed." | Passed |

### 11. Adding a vote on the discussion in the space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space that you are enrolled in on the home screen | The user navigated to the space details screen | - |
| Scroll to the Discussion section | - | - |
| Click the upvote or downvote button on a discussion | - | - |
| - | upvote/downvote icon becomes blue, vote count increases | passed |

### 12. Failed to add a vote on the discussion in the space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on a space that you are enrolled in on the home screen | The user navigated to the space details screen | - |
| Scroll to the Discussion section | - | - |
| Click the upvote or downvote button on a discussion | - | - |
| - | The vote could not be added, and the error "Vote failed" is shown in the error dialog to the user | passed |

## HOME SCREEN

### 1. Loading all trending spaces test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| - | The loading dialog is shown while loading the spaces | - |
| - | All retrieved spaces are shown in cards on the screen | Passed |

### 2. Loading all new spaces test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on the new tab on the screen | - | - |
| - | The loading dialog is shown while loading the spaces | - |
| - | All retrieved spaces are shown in cards on the screen | Passed |

### 3. Failed to load all trending spaces test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| - | The loading dialog is shown while loading the spaces | - |
| - | Spaces could not be loaded | - |
| - | An error dialog is shown on screen with the service response about the issue | Passed |

### 4. Failed to load all new spaces test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| - | The loading dialog is shown while loading the spaces | - |
| - | Spaces could not be loaded | - |
| - | An error dialog is shown on screen with the service response about the issue | Passed |

### 5. Search spaces test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| - | The loading dialog is shown while loading the spaces | - |
| - | All retrieved spaces are shown in cards on the screen | - |
| Enter a search query in the search bar | text-based search begins (tags, descriptions, titles) | - |
| - | Matching spaces are listed on screen | Passed |

### 6. Join space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| - | The loading dialog is shown while loading the spaces | - |
| - | All retrieved spaces are shown in cards on the screen | - |
| Click the Join space button on a space card | user joins the space | - |
| - | user is navigated to the space details | Passed |

### 7. Failed to join space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| - | The loading dialog is shown while loading the spaces | - |
| - | All retrieved spaces are shown in cards on the screen | - |
| Click the Join space button on a space card | The user could not join the space | - |
| - | The error message "Join failed" is shown inside the error dialog to the user | Passed |

### 8. Leave space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| - | The loading dialog is shown while loading the spaces | - |
| - | All retrieved spaces are shown in cards on the screen | - |
| Click the Leave space button on a space card | user leaves the space | - |
| - | user stays in the same page | Passed |

### 9. Failed to leave space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| - | The loading dialog is shown while loading the spaces | - |
| - | All retrieved spaces are shown in cards on the screen | - |
| Click the leave space button on a space card | The user could not leave the space | - |
| - | The error message "Leave failed" is shown inside the error dialog to the user | Passed |

### 10. See space details test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| - | The loading dialog is shown while loading the spaces | - |
| - | All retrieved spaces are shown in cards on the screen | - |
| Click on one of the space cards | User navigated to the space details screen | Passed |

## SPACE NODES

### 1. Loading all nodes of the space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| - | Nodes are retrieved from the service | - |
| - | The loading dialog is shown on screen while the request is in progress | - |
| - | The retrieved nodes is listen on the screen | Passed |

### 2. Failed to load all nodes of the space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| - | Nodes could not be retrieved from the service | - |
| - | The loading dialog is shown on screen while the request is in progress | - |
| - | An error dialog is shown on screen with the message "Failed to load nodes." | Passed |

### 3. Searching nodes of the space test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| - | Nodes are retrieved from the service | - |
| - | The loading dialog is shown on screen while the request is in progress | - |
| - | The retrieved nodes is listen on the screen | - |
| Enter a search query in the search bar | Nodes are filtered with the search query (node name, node location) | - |
| - | The resulting list is shown on screen | Passed |

### 4. Ordering the nodes of the space with respect to the date_asc test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| - | Nodes are retrieved from the service | - |
| - | The loading dialog is shown on screen while the request is in progress | - |
| - | The retrieved nodes is listen on the screen | - |
| Click the sort/filter icon button at the top right | Sort dropdown opens | - |
| Select "Date (Oldest First)" from the dropdown | Nodes are ordered by their creation date | Passed |

### 5. Ordering the nodes of the space with respect to the date_desc test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| - | Nodes are retrieved from the service | - |
| - | The loading dialog is shown on screen while the request is in progress | - |
| - | The retrieved nodes is listen on the screen | - |
| Click the sort/filter icon button at the top right | Sort dropdown opens | - |
| Select "Date (Newest First)" from the dropdown | Nodes are ordered by their creation date | Passed |

## SPACE NODE DETAILS

### 1. Getting node details test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| - | Nodes are retrieved from the service | - |
| - | The loading dialog is shown on screen while the request is in progress | - |
| - | The retrieved nodes are listed on the screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Node details are fetched from the service | - |
| - | The loading dialog is shown while retrieving the details | - |
| - | Retrieved details are shown on screen | Passed |

### 2. Failed to get node details test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| - | Nodes are retrieved from the service | - |
| - | The loading dialog is shown on screen while the request is in progress | - |
| - | The retrieved nodes are listed on the screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Node details could not be fetched from the service | - |
| - | The loading dialog is shown while retrieving the details | - |
| - | An error dialog is shown on screen with the message "Failed to get node details." | Passed |

### 3. Delete node test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| Click the menu icon (three dots) at the top right | The node options dropdown opens | - |
| Click "Delete Node" from the menu | The delete confirmation dialog appears | - |
| Confirm deletion | node deleted, and user navigated to the node list screen | Passed |

### 4. Failed to delete node test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| Click the menu icon (three dots) at the top right | The node options dropdown opens | - |
| Click "Delete Node" from the menu | The delete confirmation dialog appears | - |
| Confirm deletion | The node could not be deleted, and the user stays on the same screen | Passed |

### 5. Searching for a node property test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Retrieved details are shown on screen | - |
| Scroll down to the "Edit Node Properties" tab | - | - |
| Type "instance of" in the search box | The search result is shown inside the box | Passed |

### 6. Adding a new property to the node test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Retrieved details are shown on screen | - |
| Scroll down to the "Edit Node Properties" tab | - | - |
| Type "instance of" in the search box | The search result is shown inside the box | - |
| Check the box next to the property name | The box is checked | - |
| Click on the "Save Property" button | Property is added and is shown on the Node Properties tab | Passed |

### 7. Failed to add a new property to the node test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Retrieved details are shown on screen | - |
| Scroll down to the "Edit Node Properties" tab | - | - |
| Type "instance of" in the search box | The search result is shown inside the box | - |
| Check the box next to the property name | The box is checked | - |
| Click on the "Save Property" button | Property could not be added | - |
| - | An error message is shown on error dialog on the screen | Passed |

### 8. Deleting a new property from the node test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Retrieved details are shown on screen | - |
| Scroll down to the "Node Properties" tab | - | - |
| Click on the trash icon next to the property | The property is deleted from the node | - |
| - | The success message is shown below the screen as a toast message | Passed |

### 9. Failed to delete a new property from the node test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Retrieved details are shown on screen | - |
| Scroll down to the "Node Properties" tab | - | - |
| Click on the trash icon next to the property | The property could not be deleted from the node | - |
| - | An error message is shown on error dialog on the screen | Passed |

### 10. Update node location test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Retrieved details are shown on screen | - |
| Click on the "Edit Location" button | Location Dialog opens | - |
| Select the country "Turkey" from the dropdown list | The country is selected | - |
| Select the city "Istanbul" from the dropdown list | The city is selected | - |
| Click the "Get Coordinates from Address" button | Coordinates are retrieved from the service | - |
| Click the "Save Changes" button | Location is updated and shown on the node details screen | Passed |

### 11. Get node connections test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Retrieved details are shown on screen | - |
| Click the "Connections" tab | User navigated to the node connections screen | - |
| - | Node connections are retrieved from the service | - |
| - | The loading dialog is shown on screen while retrieving the connections | - |
| - | Retrieved results are shown on screen | Passed |

### 12. See edge details test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Retrieved details are shown on screen | - |
| Click the "Connections" tab | User navigated to the node connections screen | - |
| - | Node connections are retrieved from the service | - |
| - | The loading dialog is shown on screen while retrieving the connections | - |
| - | Retrieved results are shown on screen | - |
| Click the "See details" button for an edge | Edge details are retrieved from the service | - |
| - | The loading dialog is shown on screen while retrieving the edge details | - |
| - | Edge details are shown on screen | Passed |

### 13. Failed to retrieve edge details test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| - | Retrieved details are shown on screen | - |
| Click the "Connections" tab | User navigated to the node connections screen | - |
| Click the "See details" button for an edge | Edge details could not be retrieved from the service | - |
| - | An error message is shown on error dialog on the screen | Passed |

### 14. Update edge details test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| Click the "Connections" tab | User navigated to the node connections screen | - |
| Click the "See details" button for an edge | Edge details are retrieved from the service | - |
| Change edge label | Wiki search is being done in every change | - |
| - | Matched results are listed below the edit text box | - |
| Select an option from the list | edge label changed | - |
| Change edge direction | button color and text direction changed | - |
| Click the "Update Edge Details" button | The success message is shown on the screen as a toast message | Passed |

### 15. Delete edge test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click see details button on a node | The user navigated to the node details screen | - |
| Click the "Connections" tab | User navigated to the node connections screen | - |
| Click the "See details" button for an edge | Edge details are retrieved from the service | - |
| Click the menu icon (three dots) at the top right | Edge options open as a dropdown | - |
| Click the "Delete Edge" button | Confirm deletion opens on the screen | - |
| Click the "Yes" button | edge deleted | - |
| - | User is directed to the edge list screen | Passed |

## ADD NODE

### 1. Add node test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards that you enrolled in | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click the "Add Node" button at the top right corner | The user navigated to the add new node screen | - |
| Enter a search query in the "Search for entity" field (for example, "Galatasaray") | Search button enabled | - |
| Click the "Search" button | The loading dialog is shown on screen while the search is in progress | - |
| Select one of the options listed below the search edit text box | The selected entity text area appears on the screen | - |
| - | Related properties are shown on screen for the selected entity | - |
| Select related and available properties for the node | selected properties has checked box next to them | - |
| Click the "Create Node" button | The loading dialog is shown on the screen while the service process is being done | - |
| - | Node added to the space, and the user navigated to the node list screen | Passed |

### 2. Add node with connection test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards that you enrolled in | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click the "Add Node" button at the top right corner | The user navigated to the add new node screen | - |
| Enter a search query in the "Search for entity" field (for example, "Galatasaray") | Search button enabled | - |
| Click the "Search" button | The loading dialog is shown on screen while the search is in progress | - |
| Select one of the options listed below the search edit text box | The selected entity text area appears on the screen | - |
| - | Related properties are shown on screen for the selected entity | - |
| Select related and available properties for the node | selected properties has checked box next to them | - |
| Click on the "Select a node" box | Other nodes in the space will be listed in the shown dropdown | - |
| Select one node to connect with the current node | The selected node name is written in the box | - |
| Select the direction of the added connection | The button color and text are changed with respect to the selected direction | - |
| Type edge label | Wiki search is being done on each character change | - |
| Select one of the options from the result list for the edge label | The edge label is written on the label text box | - |
| Click the "Create Node" button | The loading dialog is shown on the screen while the service process is being done | - |
| - | Node added to the space, and the user navigated to the node list screen | Passed |

### 3. Add node with location test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards that you enrolled in | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click the "Add Node" button at the top right corner | The user navigated to the add new node screen | - |
| Enter a search query in the "Search for entity" field (for example, "Galatasaray") | Search button enabled | - |
| Click the "Search" button | The loading dialog is shown on screen while the search is in progress | - |
| Select one of the options listed below the search edit text box | The selected entity text area appears on the screen | - |
| - | Related properties are shown on screen for the selected entity | - |
| Select related and available properties for the node | selected properties has checked box next to them | - |
| Click the "Add Location" button | The location dialog is shown on the screen | - |
| Select country "Turkey." | The selected country is written in the box | - |
| Select City as "İstanbul" | The selected city is written in the box | - |
| Click the "Get Coordinates from Address" button | Address details retrieved from the service | - |
| Click the "Save" button | Location is saved | - |
| Click the "Create Node" button | The loading dialog is shown on the screen while the service process is being done | - |
| - | Node added to the space, and the user navigated to the node list screen | Passed |

### 4. Failed to add node test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click on one of the space cards that you enrolled in | User navigated to the space details screen | - |
| Click the "See Space Graph" button | The user navigated to the space nodes screen | - |
| Click the "Add Node" button at the top right corner | The user navigated to the add new node screen | - |
| Enter a search query in the "Search for entity" field (for example, "Galatasaray") | Search button enabled | - |
| Click the "Search" button | The loading dialog is shown on screen while the search is in progress | - |
| Select one of the options listed below the search edit text box | The selected entity text area appears on the screen | - |
| - | Related properties are shown on screen for the selected entity | - |
| Select related and available properties for the node | selected properties has checked box next to them | - |
| Click the "Create Node" button | The loading dialog is shown on the screen while the service process is being done | - |
| - | Node could not be added to the space, and the user stays on the same screen | - |
| - | An error dialog is shown on the screen with the message "Node creation failed." | Passed |

## ACTIVITY STREAM

### 1. Load activity stream test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click the "Activity Stream" option in the bottom navigation bar | The user navigated to the activity stream screen | - |
| - | activity stream fetches the activities | - |
| - | The loading dialog is shown on the screen while handling the fetch operation | - |
| - | Retrieved activities are listed on the screen | Passed |

### 2. Failed to load activity stream test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click the "Activity Stream" option in the bottom navigation bar | The user navigated to the activity stream screen | - |
| - | activity stream fetches the activities | - |
| - | The loading dialog is shown on the screen while handling the fetch operation | - |
| - | Activities could not be retrieved | - |
| - | An error dialog is shown on the screen with the message "Network error." | Passed |

### 3. No activity found test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click the "Activity Stream" option in the bottom navigation bar | The user navigated to the activity stream screen | - |
| - | activity stream fetches the activities | - |
| - | The loading dialog is shown on the screen while handling the fetch operation | - |
| - | Zero activity found | - |
| - | "No activities found" message is displayed on the screen | Passed |

### 4. Refreshing the activity stream screen test steps

| STEP | EXPECTED BEHAVIOR | RESULT |
|------|-------------------|--------|
| Open the application | Login screen displayed | - |
| Log in with valid credentials | user navigated to the home page | - |
| Click the "Activity Stream" option in the bottom navigation bar | The user navigated to the activity stream screen | - |
| - | activity stream fetches the activities | - |
| - | The loading dialog is shown on the screen while handling the fetch operation | - |
| - | Retrieved activities are listed on the screen | - |
| Click the "Refresh" button at the top right of the screen | Fetching activities from the service | - |
| - | The loading dialog is shown on the screen while fetching the activities | - |
| - | Refreshed activities are loaded and shown on the screen | Passed |


# Individual Contributions

## Esra Nur Özüm

### Development Contributions

| Requirement Number | Description | Issue Link | PR Link or Commit Link | Unit Test Targets | 
|--------------------|-------------|------------|---------|-------------------|
| [1.1](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | The goal of this task is to create a fully functional registration screen where users can sign up by providing their personal details           | [#24](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/24)          | [PR#26](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/26)       | [handled in issue #32](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/32)                 |
| [1.1](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | The purpose of this task is to develop a functional and user-friendly login screen that allows users to securely access their accounts           | [#25](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/25)          | [PR#33](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/33)       | [handled in issue #32](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/32)                 |
| [1.1](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | The Login and Register functionalities form the foundation of the Android application’s authentication system.This task involves creating and executing comprehensive unit tests to validate input handling, API integration, error messages, and navigation flows           | [#32](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/32)          | [Commit#bc58e45](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/commit/bc58e452f10afd3443f4d784e2c15fa8dff161ee)       | input validation, page navigations, blank or empty inputs                |
| [1.3](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | This task involves implementing a sample graph UI view for the Space Page using dummy data. The view will display nodes and edges to represent sample relationships (e.g., between users, spaces, or topics). The purpose is to provide a visual preview of the final graph structure and layout before real data integration           | [#57](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/57)          | [Commit#525bef9](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/commit/525bef91e152a2370ca2fea31b6ebcb6d54994f0)       | -                 |
| [1.3.1](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | The main objective of this task is to create the required Space Creation page and ensure successful navigation between pages           | [#79](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/79)          | [PR#88](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/88)       | -               |
| [1.3.1](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | The Space Creation flow from the existing web application needs to be fully integrated into the mobile application. While the UI components and navigation are already defined, this task focuses on connecting the frontend UI to the existing backend endpoints, enabling a fully functional Space Creation feature           | [#80](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/80)          | [PR#95](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/95)       | retriving wikidata tag search, retrieving empty result from wikidata, empty space details inputs, api error handlings, button functionalities, creating spcae with multiple or no tags                  |
| [1.3](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | A new Space Details Screen needs to be created in the mobile application to display detailed information about a selected space. This page will present the space’s core information, allow navigation to related sections, and manage discussion threads with pagination and comment interactions           | [#81](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/81)          | [PR#84](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/84)       | -                 |
| [1.3](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | This task involves connecting the frontend UI elements created in the previous task to corresponding backend endpoints, handling loading and error states, and ensuring data consistency after user interactions           | [#97](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/97)          | [PR#109](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/109)       | retrieving success result, handling api errors                 |
| [1.7](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | The task includes both UI implementation and backend integration to fetch and display live feed data using the existing API endpoints from the web application           | [#112](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/112)          | [PR#114](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/114)       | retrieving result from the api, error handling, tab seelction behaviors (new and trending), text based searching with respect to username-tag-title-description of the space                 |
| [1.4](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | For each space, we are getting nodes and edges from the backend. In the mobile application, we need to display this structured graph in a list format, allowing the user to view the list of nodes that the graph currently contains and the edges associated with each node.          | [#128](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/128)          | [PR#133](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/133)       | -                 |
| [1.4](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | This task involves the implementation of API endpoints to retrieve the related nodes and edges for the specific space           | [#132](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/132)          | [PR#147](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/147)       | success result handling, error result handling, authentication check, null response handling, edge adding, checking inputs, update edge details behavior check, delete edge behavior check, wikidata search check, add node behaivor check, delete node behavior check, update node behavior check                 |
| [1.3.12](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | There are some missing UI components for UI. And also, since the backend is ready to use for reporting functionality, we should add necessary endpoints and implement a fully functional reporting in the application           | [#156](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/156)          | [PR#157](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/157)       | retrieving report reasons api result check, error handling, report submit button behavior check, handling null results                |
| [1.12](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | An activity stream is a chronological list of actions or events that have occurred within a system, application, or platform. It typically shows who did what and when. It is implemented in the web and presented in CM2. Now I need to implement the same functionality in Android. This task involves the implementation of all UI components that will be needed for future usage.           | [#173](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/173)          | [PR#178](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/178)       | error handling, success api result handling, null api result handling, refresh button behavior handling, result limit handling, navigation handling              |
| [1.12](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | Backend endpoints are implemented and being actively used in the web. Same endpoints should be added to mobile and should be used in the activity stream page to retrieve all entries related to system updates and space updates           | [#174](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/174)           | [PR#178](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/178)       | error handling, success api result handling, null api result handling, refresh button behavior handling, result limit handling, navigation handling                 |
| [1.12](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | As we always do, we need to write necessary unit tests to cover as much flow as possible for the activity stream implementation           | [#175](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/175)           | [PR#178](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/178)       | error handling, success api result handling, null api result handling, refresh button behavior handling, result limit handling, navigation handling                 |
| [1.4](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | During our meeting with the customer, it was requested that there should be some changes in the node list UI to indicate the importance of the nodes. Having just the list of nodes ordered by their connection count is not sufficient for the customer. This task involves some UI changes that we received as a request from the customer           | [#176](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/176)          | [PR#183](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/183)       | -                 |
| [1.2.4](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | For the archived items like space, we need to have an indicator as we have on the web           | [#177](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/177)          | [PR#183](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/183)       | checking is_archieved parameter, updated existing space unit tests                 |
| [1.3.13](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | Implement geolocation functionality for users, nodes, and spaces. Each of these entities should support storing and updating geographic coordinates (latitude and longitude). The geolocation data will be used for spatial queries, filtering, and mapping purposes           | [#184](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/184)          | [PR#209](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/209)       | retrieveing success result check, api error handling, button behavior check, updated existing unit tests for profile, node, and spaces                 |
| [1.4](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | To improve the UI/UX of the node details, the properties with the same label should be grouped and seen inside the node properties area           | [#202](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/202)          | [Commit#074d227](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/commit/074d2270d01439eb9d39d0a7bd658b52f95091b4)       | updated existing unit tests for node properties, handling grouping for same p values, handling select all checkbox behavior, handling group checkbox behavior                 |
[1.4](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)                  | In space details when adding a node users should be able select all properties easily instead of clicking 1 by 1           | [#207](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/207)          | [Commit#074d227](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/commit/074d2270d01439eb9d39d0a7bd658b52f95091b4)       | updated existing unit tests for node properties, handling grouping for same p values, handling select all checkbox behavior, handling group checkbox behavior                 |


### Documentation Contributions

| Description | Wiki Link | Issue Link | External link (if any) | 
|--------------------|-------------|------------|---------|
| To improve project management, classification, and tracking of work, we need to define and create a set of GitHub issue tags (labels). These tags will help the team categorize issues by scope, type, and priority, ensuring consistency and clarity across all development tasks. | [Project Tags](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/labels)          | [#3](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/3)         | -    |
|Research W3C accessibility standards|[1](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/ColorPalettesVisualElements), [2](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/ColorBlindnessResearch)|[#5](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/5)|-|
|Research mobile app frameworks|-|[#6](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/6)|-|
|Design drawings are required for the screens used in the Android application. The mockups should provide page previews of the main features, including graph visualization, space management, discussions, and search|[Designs](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/Mockup)|[#13](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/13)|[figma](https://www.figma.com/design/FhB4LUCMxg8UQ19lLP2FGS/ConnectTheDots?node-id=2-57&p=f&t=lksHvhUX65CZfcq0-0)|
|Creating UML diagrams|[UMLs](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/UMLDiagrams)|[#50](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/50)|-|
|Our team needs to prepare a structured Milestone Report for the first customer milestone. The template will be used collaboratively by all team members to document progress, outcomes, and deliverables in a clear and academic format suitable for submission and presentation|-|[#51](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/51)|[notion](https://www.notion.so/Milestone-Report-Template-28f41ca10d0a80a68193f257c6497777)|


####  Most complex functionality that I have worked on
* Throughout the project, I was mainly involved in the development of the mobile application. During this process, I also had the opportunity to work with an architectural approach that I do not actively use in my current professional work. At the beginning, it took some time to understand how the architecture handled state management. The most challenging period for me was the initial phase when I first moved into the development stage of the project. However, since I was initially responsible for developing more basic pages, the level of difficulty gradually decreased over time. In later stages, I started to face challenges again, particularly during the implementation of functionalities related to space. Both the complexity of the concept itself and the need to progress as parallel as possible with the web side introduced several constraints. Additionally, because we were using a relatively complex architectural model, managing states and handling different cases in a growing project became challenging. One of the screens where I spent the most time and faced the most difficulty was the home page. Due to the complex and heavy response structures received from the API, as well as the impact of button state changes on the screen flow, managing the application flows became particularly challenging.
* <img width="200" height="400" alt="image" src="https://github.com/user-attachments/assets/d27a8231-440d-4df2-a19e-1883131921c7" />

### Code Reviews

| Review Link | Review Status | Development Owner| Conversation | 
|-------------|---------------|-------------------|--------------|
|[#14](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/14)| Approved | Yasemin Tangül |-|
|[#19](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/19)| Approved | Gamze Güneri |-|
|[#21](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/21)| Approved | Yusuf Bayam |-|
|[#34](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/34)| Approved | Yusuf Bayam |-|
|[#38](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/38)| Approved | Yusuf Bayam |Change requested and after the updates PR is approved|
|[#49](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/49)| Approved | Batuhan Cömert |-|
|[#77](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/77)| Approved | Yasemin Tangül |-|
|[#98](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/98)| Approved | Gamze Güneri |-|
|[#103](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/103)| Approved | Yusuf Bayam |-|
|[#106](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/106)| Approved | Yasemin Tangül |-|
|[#120](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/120)| Approved | Batuhan Cömert |-|
|[#123](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/123)| Approved | Yusuf Bayam |-|
|[#155](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/155)| Approved | Batuhan Cömert |-|
|[#163](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/163)| Approved | Yasemin Tangül |Pre-release PR|
|[#170](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/170)| Approved | Yasemin Tangül |Grafana Configuration PR for release|

## Yasemin Tangül

# Advanced Graph Search

## Related Requirements: 
### In App Search:
1.5.4. The system shall provide advanced search functionality within spaces that allows users to construct complex queries using node properties, edge properties and specifying relation level between queries.

1.5.5. The system shall display advanced search results in a graph layout which highlights the path between results and a list of results including nodes, edges and properties.

## Description
In the space graph, the system must be provide search based on the node, edge, property and if property is selected including property value. In order to achieve this, graph database Neo4j is deployed & implemented to application. Neo4j is running on a different ec2 instance(different server), while the node creation/update/delete or space creation/delete/update does the same to both PostgreSQL and Neo4j. Means the data both stored in graph database and structured database. In the application, node, edge, property and if property selected property value can be search with multiple different combinations or each of them can be searched individually.

## Related Issues

Graph Database Implementation : [#206](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/206)

Postgresql to Neo4j Migration : [#246](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/246)

Highligting the Graph Search Result : [#247](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/247)

Search bar of Dropdown Checklist and Graph Search Results : [#248] (https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/248)

## URLS

Neo4j Deployment : `http://13.60.235.0:7474/browser/`   -> Click Database from the left corner

Pull Request : https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/245
 
The important source code in the pull request is the neo4j_db : [Link to source code in pr](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/245/files#diff-f93663f0219981e89f489595bca03de2cf32ecdd8861d657f59cf8cc0d0760e0) -> Click to load diff 

In order to use the current data in the postgresql, migration to the neo4j is necessary script is migrate_to_neo4j : [Link to source code in pr](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/245/files#diff-b50e59d992f5c45b0fe2e27ab98d820864e62e3909fe6b6f686dd6b3af7fc2a6)


## Development Process
Neo4j is deployed on AWS EC2 with using the same security group of test and production servers. In this way, instances are able to communicate without extra network configuration.

Accessable in : Neo4j Deployment : `http://13.60.235.0:7474/browser/` 

<img width="1586" height="307" alt="Screenshot from 2025-12-20 21-51-49" src="https://github.com/user-attachments/assets/176945b2-8ee3-4adb-bfca-bc28eb3b11b5" />

One of the major blocker was our previous data was in postgresql and creating that much data from scratch for Neo4j will be to be taking too much time. In order to solve this blocker, I migrated the data from our production server's (13.60.88.202:5432) postgresql to neo4j. For this purpose migrate_to_neo4j.py is used : https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/blob/main/backend/migrate_to_neo4j.py

The below example shows previously created space's migrated version to Neo4j.

<img width="575" height="395" alt="Screenshot from 2025-12-20 21-57-20" src="https://github.com/user-attachments/assets/d3177719-4f62-4ca1-8a72-3851eaf692ae" />

The space,node,edge and property functionality in the Neo4j side is the same as the PostgreSQL side. While saving/deleting/updating something on the postgresql side (e.g : node creation,space deletion) the same operation is applied to neo4j. The mapping between postgresql with neo4j is done by the node_id See the source code: [Line 45, neo4j_db.py](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/blob/main/backend/api/neo4j_db.py)

For example for the synronized node creation to both postgresql and noeo4j see the source code : [Line 598-631 views.py](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/blob/main/backend/api/views.py)

Another example the edge creation, see the source code: [Line 698-715 view.py](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/blob/main/backend/api/views.py)

For the graph searching algorithm, the logic is:

1- If only node selected -> Depth 1-5 brings the neighbours of the node. The nodes directly connected to the searched node are in the depth 1.

2- If only edge selected -> The nodes connected to the edge source and the target (->) are in the depth level 1. If the depth level is increased, it is going to be showing the edge + node duo for the previously connected node.

3- If both edge and node selected it is basically the previous 2 logics merged version. 

4- For the only property search, if any node is containing that property is it going to be highlighted with green and label of "Property" onder the node. The depth is applicable for the only property search.

5- For the property value search, selection of the property is a must, if any node is containing that property value, those nodes are going to be highlited with yellow and label of "Property Value" under the node. In this case, even a node has the same property but different property value those nodes are not showing. Only the exact match of nodes contains property and property value duo are shown in the subgraph. Depth is applicable for the property value search since the value is directly points to a node.

For the code version of the algorithm see the source code : [Line 257-418, neo4j_db.py](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/blob/main/backend/api/neo4j_db.py)

## Advanced Graph Search Demo

### Node Search & Depth Logic

Only 1 node is selected from the graph, the below image show the depth 1, means directly connected nodes to the searched node. The searched node has a yellow indicator on the upper right.

<img width="1375" height="823" alt="Screenshot from 2025-12-20 22-33-24" src="https://github.com/user-attachments/assets/2435e0cc-bcd4-40c2-b72b-eef1140b7371" />

Under the subgraph,as searched query results nodes, edges, properties are also listed.

<img width="1246" height="584" alt="Screenshot from 2025-12-20 22-35-18" src="https://github.com/user-attachments/assets/df1a245c-5ad1-4808-b620-cfe192808f94" />

When the depth is 2, the subgraph will be include the neighbours of the first level nodes 

<img width="1323" height="849" alt="Screenshot from 2025-12-20 22-37-44" src="https://github.com/user-attachments/assets/24a65d4d-537d-409f-b348-bde3254dc1f9" />

Same logic is applicable for the other depth levels.

### Edge Search & Depth Logic

Only 1 edge is selected with depth level 1. The graph show the source and target nodes of the searched edge. The searched edge is indicated with red in the subgraph

<img width="1298" height="835" alt="Screenshot from 2025-12-20 22-39-04" src="https://github.com/user-attachments/assets/dfa0394a-5e55-49f5-beb6-d87bb4422588" />

In the depth level 2, the next edge and node duo is also included to the subgraph. For example the "radio operator" was in the level 1 as one of the direct connection of the edge. Since the below example is the depth 2, while in edge and North America node is included to the subgraph. Same logic applies to the other levels.

<img width="1315" height="851" alt="Screenshot from 2025-12-20 22-40-22" src="https://github.com/user-attachments/assets/3bfc88e2-22fd-42bd-a579-8fdde177f4ca" />

### Propert Search & Depth Logic

If only a property is selected, graph will be showing the nodes if that property is in that node. 

<img width="1331" height="858" alt="Screenshot from 2025-12-20 22-43-13" src="https://github.com/user-attachments/assets/6ddcb98e-ce1e-4580-ad5a-3c62fd713cf5" />

If also property value is selected along with the property, the subgraph will be showing the node if node has that property value. If any other node is containing the property but not the property value those are ignored.

<img width="1300" height="845" alt="Screenshot from 2025-12-20 22-49-45" src="https://github.com/user-attachments/assets/d43c9dd5-383a-4027-8cd1-64a2522396c4" />

# CI/CD Pipeline 

<img width="1781" height="807" alt="Screenshot from 2025-12-20 23-11-55" src="https://github.com/user-attachments/assets/bdf52904-51a3-425e-ab86-a37e7fb353b1" />

## Related Requirements

### Reliability:
2.2.1. The system shall ensure 99.9% uptime.

### System Architecture:
3.1.1. The system shall be available as a web & Android application.
3.1.4. The system shall have two different deployment environments: one for development & testing and one for production.
3.1.5. The system shall be deployed on an AWS EC2 Instance.
3.1.6. The system for the Android client shall be distributed as an APK through Firebase App Distribution.


## Description
In order to main quality code, versioning, continues improvement and continues deployment CI/CD pipelines are builded as one of the first steps of the project. Since the pipelines are always evaluating, and making the deployment phase automatically, a deployment of the version of the application was never a problem.

## Related Issues

Set Up CI Pipeline : [#1](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/1)

Implement Code Coverage Reporting : [#72](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/72)

EC2 Instance IP Update : [#106](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/106)

Migration Index Conflict : [#161](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/161)

Resource Increase on the test and production servers : [#242](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/242)
## URLS

Currently there are 6 different pipelines are in the use, 3 for the web application (feature,develop and main) and 3 for the mobile application (feature,develop and main)

See the github pipelines in : [.github/workflows](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/tree/main/.github/workflows)

## Development Process

### Web:
For the web application 2 different AWS EC2 instance are created in order to maintaing always working code on the production server.

Test: `16.171.44.79:3000`

Prod: `13.60.88.202:3000`

If develop branch or main branch is triggered by push, the code will be tested for the followings:

* Django migration conflicts (only for develop)
* Unit tests
* If the application is able to run with the docker compose
* Smoke tests
 
If those testing stages are passed, then , ci will whilelist of the current runner's ip on the AWS security group, pulls the current repository to server than runs it via docker-compose.

During the CI, the tests results are commented on the action run.

### Mobile:

For the feature,develop and main branch if anything under the mobile/ folder or the directly the ci/cd's itself, the mobile pipelines are triggered(depends on the branch).

For the feature branch : alfa tagged .apk is builded
For the develop branch : beta tagged .apk is builded

And for the main, directly builded with the commit sha.

The below example shows the beta one(no issue task for the develop branch push that is why starts with unknown):

<img width="1819" height="906" alt="Screenshot from 2025-12-20 23-13-01" src="https://github.com/user-attachments/assets/474b469d-8963-473b-8d31-ad95f46399e5" />

Test results for each run:

<img width="1815" height="935" alt="Screenshot from 2025-12-20 23-17-13" src="https://github.com/user-attachments/assets/462a25db-8b94-4b01-a042-f9d11de868fe" />

Code coverage for each run(only web):

<img width="1801" height="1009" alt="Screenshot from 2025-12-20 23-17-43" src="https://github.com/user-attachments/assets/6dfb56d0-5721-4a8b-b77b-e320409b6d24" />


# Other Works:

Admin Dashboard (Backoffice Analytics):

Issue : [#145](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/145)

(Currently not working on django, can be checked from grafana) 

Grafana: http://13.60.88.202:3001/dashboards

Space Analytics:

<img width="1813" height="1003" alt="Screenshot from 2025-12-20 23-24-01" src="https://github.com/user-attachments/assets/bafc239e-19f9-45b6-819c-d23b60cb8784" />

Issue : [#121](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/121)

URL: http://13.60.88.202:3000/spaces/1/analytics
Geolocation Implementation and Map Visualization : 

<img width="1818" height="980" alt="Screenshot from 2025-12-20 23-25-38" src="https://github.com/user-attachments/assets/63b3c87c-73c8-4c72-9fd1-40f96d226924" />


Issue : [#113](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/113)

# Batuhan Cömert 

## Executive Summary
My contributions span three core areas:

- **A) Research & Specification (Accessibility / WCAG / W3C / AS2):** I translated SRS accessibility requirements into implementable engineering guidance, focusing on WCAG contrast/color-use rules and W3C patterns for activity streams (AS2 data model + ARIA feed presentation). This work informed both UI consistency and activity stream implementation choices.
- **B) Implementation (Theme + Activity Stream End-to-End):**
  - **B1:** Implemented and refactored a WCAG-oriented global color theme approach (reducing hard-coded styling, improving maintainability via variables/DRY).
  - **B2:** Delivered an end-to-end Activity Stream foundation (AS2-like activity recording + endpoint + UI + tests) and incorporated code review feedback to improve correctness.
- **C) Implementation (Advanced Search Backend API):** Implemented a structured backend API for advanced search within a space, which later evolved into a different approach after instructor feedback.

**All referenced artifacts (issues/PRs/wiki/docs/tests) are linked throughout and summarized in tables at the end.**

---

# A) Research & Specification — Accessibility (Color Blindness + WCAG/W3C + Activity Stream)

## Related requirements (SRS RQ-2.4 Accessibility)
Focused primarily on:
- **2.4.3** The system shall provide an activity stream in compliance with W3C standards.
- **2.4.4** The system shall present content in a format that is accessible and easily distinguishable for users with color vision deficiency.
- Plus WCAG 2.1/2.2 guidance supporting contrast and “use of color”.

## Brief description
- Researched and documented WCAG requirements as **implementable thresholds**:
  - **WCAG 2.2 — 1.4.3 (AA) Contrast (Minimum)**: normal text ≥ 4.5:1, large text ≥ 3:1.
  - **WCAG 2.1 — 1.4.11 (AA) Non-Text Contrast**: UI components (borders, focus rings, meaningful icons) ≥ 3:1.
  - **WCAG — 1.4.1 (A) Use of Color**: information must not be conveyed by color alone (must include non-color cues such as icons/text/patterns).
- Produced a **color-blind-friendly palette research and testing approach**, including listing tools/methods for verifying accessibility.
- Defined Activity Stream in two layers:
  - **Data layer:** ActivityStreams 2.0 (AS2)-compatible event representation.
  - **Presentation layer:** WAI-ARIA feed pattern guidance (role="feed" and role="article") for accessible display.

## Related issues
- **#11** Specify and document detailed accessibility features in compliance with W3C standards (SRS 2.4.3–2.4.7)  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/11
- **#20** Research color palettes and visual elements for color blindness accessibility (WCAG 2.1)  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/20

## URLs to relevant content
- Wiki — **ColorBlindnessResearch**  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/ColorBlindnessResearch
- Wiki — **ColorPalettesVisualElements** (project-wide palette & visual elements reference)  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/ColorPalettesVisualElements
- CM1 Report (Google Doc) — **“W3C Research”** section  
  https://docs.google.com/document/d/1lAZA7_YY-vgzgnV3AXV0md7P0MZWCBHrzz-1k3R0wc0/edit?tab=t.0#heading=h.k5bh05shkyrl

## Challenges
- Converting broad WCAG/W3C guidance into **developer-ready rules** (thresholds + component-level implications).
- Ensuring “use of color” requirements are **practically enforceable** across UI patterns (errors/success/warnings, node legends).
- Designing Activity Stream as both a **portable data model (AS2)** and an **accessible UI pattern (ARIA feed)**.

---

# B) Implementation — Theme + Activity Stream (End-to-End)

## B1) Global Color Palette / Theme — WCAG-oriented implementation & DRY refactor

### Related requirement
- Supports **RQ-2.4.4** (color vision deficiency accessibility) and aligns with WCAG contrast guidance used throughout the UI.

### Brief description
- Contributed to implementing a consistent, accessibility-aware theme across the app.
- Incorporated review feedback to avoid hard-coded colors and to use a maintainable variable-driven approach.
- Addressed workflow correctness (branch naming) based on teammate feedback and re-aligned with the documented development workflow.

### Related issue
- **#30** Implement a WCAG-compliant color theme applied globally and switchable  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/30

### Related PRs / references
- **PR #40** (workflow/branch naming correction + styling changes driven by review feedback)  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/40
- **PR #61** Color palette implementation for web refactored for DRY code  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/61
- Development workflow reference  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/DevelopmentWorkflow
- Design reference (palette/visual elements)  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/ColorPalettesVisualElements

### Review / communication outcome 
- PR #40 included feedback to (1) follow workflow branch naming and (2) avoid hard-coded colors; I corrected my approach accordingly in subsequent work.  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/40

---

## B2) Activity Stream — AS2-like recording + feed endpoint + UI (+ tests)

### Related requirement
- Directly implements **RQ-2.4.3** (W3C-compliant activity stream) via an AS2-like model and ActivityStreams response structure.

### Brief description
- Implemented a backend foundation where each mutation (spaces, nodes, edges, discussions, reactions, snapshots) records an **AS2-like activity row**.
- Added the **GET /activity-stream** endpoint returning an ActivityStreams-compatible collection page with filters and pagination.
- Implemented the frontend Activity Stream UI component and styles and ensured test coverage exists in the PR files.
- Incorporated code review feedback regarding correctness (timing/order of activity creation around destructive operations).

### Related issues
- **#137** Implement ActivityStreams 2.0 Recording Mechanism  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/137
- **#139** Implement Activity Feed Endpoint  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/139
- **#140** Implement UI for Activity Feed  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/140

### Related PRs
- **PR #141** Adds an ActivityStreams 2.0–compatible “outbox” recording backend mutations  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/141
- **PR #150** Feature/137 as2 recording mechanism (includes fixes based on review feedback)  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/150
- **PR #155** Activity feed endpoint (`GET /activity-stream`) with filters + pagination, ActivityStreams OrderedCollectionPage response  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/155
- **PR #166** Add Activity Stream component and related styles, implement activity  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/166

### Tests
- Tests are available in PR #166 files (diff link):  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/166/files#diff-7bfbc7cec0b1920a351bb77d45366414e8e869f3198dd29624cdc846f61b8073

### Code review (received) — what changed as a result
- In PR #141/#150 discussion, **Yusuf** raised a correctness concern about recording deletion activity in a way that might produce an activity record even if deletion fails; I updated the implementation accordingly in the follow-up work.  
  Fix PR: https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/150

### Challenges (concrete)
- Preserving ActivityStreams semantics while implementing practical filters/pagination in a maintainable API design (PR #155).
- Ensuring activity recording correctness around destructive operations (review-driven fix captured in PR #150).
- Keeping the UI implementation consistent with theme/styling constraints and testable behavior (PR #166 + linked tests).

---

# C) Advanced Search Backend API (Feature later evolved after instructor feedback and this version wasn't used)

## Related issue
- **#180** Implement Advanced Search for Space Backend&Frontend  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/180

## Brief description
Although the overall feature later evolved into a different version after instructor feedback (towards a Neo4j-based approach), I implemented the backend API version described in #180:
- Support structured multi-condition queries using property–value pairs and logical operators (AND/OR).
- Filter nodes by properties and exact-match values.
- Return a list of matching nodes with summary fields suitable for preview.
- Consider performance for larger spaces and handle empty queries gracefully.
- Provide test coverage scenarios as described in the issue (single condition, AND, OR, invalid input).

## Related PR
- **PR #182** Feature/180 advanced search feature (backend implementation)  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/182

## Challenges (concrete)
- Designing a query format that supports AND/OR combinations while keeping server-side validation strict enough to reject malformed constraints.
- Balancing response payload size (preview-friendly fields) with usability and performance.

---

# Other — Additional Contributions 

## 1) Interactive Activity Stream Navigation (Dynamic Activity Stream)
**Context:** The activity streams on Home and Space pages were not interactive; mentioned users and nodes were not clickable.  
**Work delivered:** Implemented clickable navigation for:
- Actors/users → `/profile/:username`
- Node mentions in multiple formats (Node:123, node:123, quoted labels, “reported X”, edge source/target)
- Home → navigate to correct space and auto-open node card via `?nodeId=...`
- SpaceDetails → open node detail modal directly
- Styling updated to use CSS variables and accessible focus/hover behaviors

- **Issue #228** Dynamic activity stream implemented  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/228
- **PR #229** Dynamic activity stream implemented  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/229

## 2) Voting Ability on Discussion (Web App)
- **Issue #82** Voting Ability on Discussion (Web App)  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/82
- **PR #83** Voting ability feature on Web space discussion added  
  https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/83

---

# Screenshots
Previous version of search:
<img width="825" height="878" alt="Ekran görüntüsü 2025-12-20 233057" src="https://github.com/user-attachments/assets/436ad897-e6d6-40b7-be56-7de33fe8c787" />

Activity stream Space:
<img width="1764" height="874" alt="Ekran görüntüsü 2025-12-20 232737" src="https://github.com/user-attachments/assets/714fcf47-f29a-461c-935e-6c44a3fe07c3" />

Activity Stream Main Page:
<img width="1764" height="874" alt="Ekran görüntüsü 2025-12-20 232737" src="https://github.com/user-attachments/assets/07a3a67f-c5ed-4796-856e-5bee37758da3" />

Vote:
<img width="314" height="539" alt="Ekran görüntüsü 2025-12-20 233828" src="https://github.com/user-attachments/assets/9b95d7da-dbc4-45a3-8730-40fa3632df98" />

---

# Tables

## Pull Requests
| PR | Title / Contribution | URL |
|---:|---|---|
| #40 | Workflow/branch correction + theme-related changes driven by review feedback | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/40 |
| #61 | Color palette implementation for web refactored for DRY code | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/61 |
| #141 | ActivityStreams 2.0–compatible “outbox” (backend activity recording foundation) | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/141 |
| #150 | Feature/137 as2 recording mechanism (review-driven correctness improvements) | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/150 |
| #155 | Activity feed endpoint `GET /activity-stream` (AS OrderedCollectionPage + filters + pagination) | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/155 |
| #166 | UI for Activity Feed: component + styles (+ tests in PR files) | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/166 |
| #182 | Feature/180 advanced search backend implementation | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/182 |
| #229 | Dynamic activity stream: clickable actors + node mentions + CSS variable styling | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/229 |
| #83 | Voting ability feature on web space discussion | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/83 |

## Issues 
| Issue | Title | URL |
|---:|---|---|
| #11 | Specify and document detailed accessibility features in compliance with W3C standards (SRS 2.4.3–2.4.7) | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/11 |
| #20 | Research color palettes and visual elements for color blindness accessibility | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/20 |
| #30 | Implement a WCAG-compliant global color theme (switchable) | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/30 |
| #137 | Implement ActivityStreams 2.0 Recording Mechanism | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/137 |
| #139 | Implement Activity Feed Endpoint | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/139 |
| #140 | Implement UI for Activity Feed | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/140 |
| #180 | Implement Advanced Search for Space Backend&Frontend | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/180 |
| #228 | Dynamic activity stream implemented | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/228 |
| #82 | Voting Ability on Discussion (Web App) | https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/82 |

---

# Documentation Links (for quick reference)
- ColorBlindnessResearch (Wiki): https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/ColorBlindnessResearch
- ColorPalettesVisualElements (Wiki): https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/ColorPalettesVisualElements
- DevelopmentWorkflow (Wiki): https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/DevelopmentWorkflow
- CM1 Report “W3C Research” (Google Doc): https://docs.google.com/document/d/1lAZA7_YY-vgzgnV3AXV0md7P0MZWCBHrzz-1k3R0wc0/edit?tab=t.0#heading=h.k5bh05shkyrl

# Individual Contributions - Yusuf

## Student Information

**Name:** Mehmet Yusuf Bayam

**Student ID:** 2024719024

**Project:** Connect The Dots

**Semester:** Fall 2025

### Executive Summary

For this semester's Connect The Dots project, I took a significant responsibility in team communication, product ownership and development aspects of the project.

For team communication, I participated actively in lectures and in online meetings. I took responsibility to bring discussions to refine the features as much as possible. I come up with options & alternative for team communication channels to establish efficient team communication flow. I also took responsibility to take meeting notes in significant number of meetings and documented them in Wikidata. I have also took responsibility in customer milestone presentations by coming up scneario ideas, preparing scenarios and presenting significant amount of features.

For product ownership aspects, I listened and analysed the needs of customer and opened discussion with the team in the meetings to drive the product to be fit to requirements. When there are some disagreements or confusion, I did my best to come up with a logical and creative solutions to drive the project in an efficient way. I created SRS and documented it in project Wiki.

From start of the semester, I have delivered 82 commits across mobile, backend, and frontend components of the project. I established the Android application foundation using Clean Architecture and MVVM patterns, including project setup, navigation systems, and comprehensive development documentation (Issue #16, PR #21). I implemented core mobile features including user profile screens, color-blind theme support, and full API integration with authentication token persistence (Issues #28, #29, #58, #64). Aside from development tasks I took significant responsibility on reviewing PRs throughout the semester on all parts of projects.

On the backend, I developed the complete reporting system with models, serializers, views, and frontend integration, including report grouping, status management, and moderation workflows (Issue #118, PR #123). I enhanced Wikidata integration by implementing property support for nodes and edges with SPARQL queries, search functionality, and centralized API headers (Issues #85, #86, #103). I also created API documentation, implemented language support infrastructure with Turkish translations, and added Gemini AI model integration (Issues #87, #124, #189).

For frontend improvements, I implemented node display enhancements with dynamic sizing, instance type filtering, property grouping, expandable node details with Wikidata images, and comprehensive admin dashboard role-based access controls (Issues #129, #179, #185, #186, #211, #213). I enhanced the reporting interface with sorting, filtering, and search functionality (Issues #164, #231, #239). These contributions address project requirements for multi-platform support, content moderation, data visualization, and user experience, while maintaining clean architecture principles and following project management practices with thorough documentation and testing.

**Related Issues:** [#16](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/16), [#28](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/28), [#29](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/29), [#58](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/58), [#64](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/64), [#85](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/85), [#86](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/86), [#87](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/87), [#103](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/103), [#118](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/118), [#124](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/124), [#129](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/129), [#164](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/164), [#179](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/179), [#189](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/189), [#211](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/211), [#213](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/213), [#231](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/231), [#239](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/239)  
**Related Pull Requests:** [#21](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/21), [#33](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/33), [#34](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/34), [#38](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/38), [#63](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/63), [#89](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/89), [#102](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/102), [#104](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/104), [#117](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/117), [#123](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/123), [#125](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/125), [#130](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/130), [#144](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/144), [#151](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/151), [#165](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/165), [#190](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/190), [#205](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/205), [#214](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/214), [#224](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/224), [#234](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/234)

---

### Significant Personal Efforts

#### Effort 1: Mobile app initialization for Connect The Dots
- **Related Requirement:** 3.1.1. The system shall be available as a web & Android application.
- **Brief Description:** Initializing Android application in Kotlin with MVVM architecture.
- **Related Issue URLs:** 
  - [#16](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/16) - Initial Mobile Application will be Started
- **Relevant Content URLs:**
  - Source Code: [PR #21](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/21/commits)

#### Effort 2: Mobile: User profile screen implementation with Unit tests
- **Related Requirement:** 1.8. User Profile
- **Brief Description:**  Provide a screen for users to view their own public information and contributions, as well as those of other users.
- **Related Issue URLs:** 
  - [#28](github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/28) - User profile screen with unit tests for mobile application
- **Relevant Content URLs:**
  - Source Code: [PR #38](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/38)
  - Documentation: [Requirement 1.8](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification#18-user-profile)
  - Tests: [ProfileViewModelTest](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/38/files#diff-f9202a2bbd48ae37c43761075ccdc59a2e5606213a9fdbcdc65b182a0d4a0e25), [EditProfileViewModelTest](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/38/files#diff-cd1c1470156e4fabbf4bb0cfff413de55fe389afc3395efcfb3019da95ec3e0e)

#### Effort 3: Backend & Web Frontend: Display property names on node creation and details
- **Related Requirement:** Add later TODO
- **Brief Description:**  System shall display properties at node creation and node details in an efficient way without any limits.
- **Related Issue URLs:** 
  - [#86](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/86) - Property names should be displayed at node creation and node details
- **Relevant Content URLs:**
  - Source Code: [PR #89](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/89)
  - Tests: [test_properties.py](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/89/files#diff-075f5bcdd9b6fcf0e22dd570df077837b989507f7e01b4a3b3e8952baaa6c8bb)

#### Effort 4: Web Frontend: Support language localization
- **Related Requirement:** 2.4.2. The system shall have localization for different languages.
- **Brief Description:**  Implementation of language localization support for English & Turkish languages using i18n.
- **Related Issue URLs:** 
  - [#87](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/87) - Language support foundation for web
- **Relevant Content URLs:**
  - Source Code: [PR #87](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/117)

#### Effort 5: Backend & Web Frontend: Report functionality
- **Related Requirement:** 
    - 1.2.11. The system shall provide a back-office dashboard for Admins to manage users, roles, and review archived or reported content.
    - 1.3.12. The user shall be able to report spaces, nodes, and user comments for inappropriate content.
- **Brief Description:**  Users shall be able to report spaces, nodes, and user comments for inappropriate content by supplying a reason. Authorized users shall be able to view this reports in backoffice. This functionality should be covered by unit tests.
- **Related Issue URLs:** 
  - [#118](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/118) - Report functionality
- **Relevant Content URLs:**
  - Source Code: [PR #123](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/123)
  - Tests: [test_reports.py](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/123/files#diff-00215049aa523ec8ed9e0f4c5dba773ef9e2c42007dbe60a7841d6d0e0495b39)

#### Effort 6: Backend & Web Frontend: AI Summary feature(Pair programming with Gamze)
- **Related Requirement:** TODO
- **Brief Description:**  System shall display an summarization of space details on demand using a LLM, given nodes, edges, statistics and contributions for the space.
- **Related Issue URLs:** 
  - [#189](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/189) - AI Summary functionality for space details
- **Relevant Content URLs:**
  - Source Code: [PR #210](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/210)

#### Effort 7: Backend & Web frontend: In space graph node display & quick filtering according to instance types
- **Related Requirement:** TODO
- **Brief Description:**  System shall display nodes with common instance of(P31) properties in different colors. Also there should be a quick filtering functionality to highlight nodes with common instance of properties.
- **Related Issue URLs:** 
  - [#211](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/211) - Node display according to instance types & quick filtering
- **Relevant Content URLs:**
  - Source Code: [PR #224](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/224)
  - Tests: [test_instance_types.py](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/224/files#diff-02356b64b2f4cade04bb1df3d90fef81ce89a45fdef3655ae235063cf2fb3b80) - [test_properties.py](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/224/files#diff-075f5bcdd9b6fcf0e22dd570df077837b989507f7e01b4a3b3e8952baaa6c8bb)

---

### Documentation

I have authored comprehensive documentation for the project, covering requirements specification, mobile development, API usage, and development workflows.

1. **Software Requirements Specification (SRS) - Wiki**
   - Location: [RequirementsSpecification Wiki Page](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)
   - Sections Written: Complete SRS document including:
     - Purpose and Scope of the Project
     - Glossary of terms
     - Functional Requirements (all sections):
       - Registration and Login (1.1)
       - Authorization (1.2)
       - Spaces (1.3)
       - Nodes and Edges (1.4)
       - Discussions (1.5)
       - Search (1.6)
       - Tags (1.7)
       - User Profile (1.8)
       - Analytics (1.9)
     - Non-Functional Requirements (2.1-2.5)
     - System Constraints (3.1-3.2)
   - Related Work: Product ownership and requirements analysis
   - Note: This comprehensive SRS document was created as part of my product ownership responsibilities and documented in the project Wiki to serve as the foundation for all development work.

2. **Mobile Application Development Guide**
   - Location: [mobile/README.md](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/blob/main/mobile/README.md)
   - Sections Written: Complete documentation including:
     - Architecture overview (Clean Architecture principles, MVVM pattern)
     - Key design patterns (State Management with ViewState, One-Time Events for Navigation, Navigation with Nested Graphs)
     - Development guidelines (Adding new screens, Adding localized strings)
     - Build and run instructions
   - Related Issue: [#16](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/16)
   - Commit: [831ae75f](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/commit/831ae75f66af1347dbe64179d26a668eeebc0e8c)

3. **API Documentation (OpenAPI/Swagger)**
   - Location: 
     - Swagger UI: Available at `/api/docs/` endpoint
     - ReDoc: Available at `/api/redoc/` endpoint
     - Schema: Available at `/api/schema/` endpoint
   - Implementation: Integrated drf-spectacular library for automatic OpenAPI 3.0 schema generation
   - Sections Created:
     - Complete API endpoint documentation for all REST endpoints
     - Request/response schemas with detailed field descriptions
     - Authentication requirements and token-based access
     - Error handling documentation with status codes
     - Interactive API testing interface
   - Related Issue: [#124](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/124)
   - Pull Request: [PR #125](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/125)
   - Code Location: [backend/backend/urls.py](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/blob/main/backend/backend/urls.py)


4. **Meeting Notes and Documentation (Wiki)**
   - Location: Project Wiki (various pages)
   - Sections Written: 
     - Meeting notes from significant number of team meetings
     - Documentation of discussions and decisions
     - Feature refinement documentation
   - Related Work: Team communication and project management responsibilities
   - Note: As mentioned in the Executive Summary, I took responsibility for taking meeting notes in significant number of meetings and documented them in the project Wiki to maintain project history and decision tracking.

---

### Demo


#### Space Graph Node Highlight and Filter by Common Instance Type
**Description:** This feature enhances the space graph visualization by displaying nodes with different colors based on their common instance type (P31 property from Wikidata) and provides a quick filtering mechanism to highlight nodes with specific instance types. The implementation fetches instance types for all nodes in a space using batch SPARQL queries, assigns distinct colors to common instance types (such as Person, Organization, Location), and allows users to filter the graph in real-time by selecting specific instance types from a filter panel. The filtering is performed client-side for instant feedback, and the graph rendering is optimized to only update node colors and visibility when filters change, rather than re-rendering the entire graph. A visual legend displays which colors correspond to which instance types, improving usability. The feature handles edge cases such as nodes with multiple instance types, nodes with no instance types, and efficiently manages state when users toggle multiple filters.

Screenshot
<img width="1471" height="684" alt="Screenshot 2025-12-20 at 23 25 02" src="https://github.com/user-attachments/assets/29856ff4-bd11-4980-bec4-72a40c57e922" />

**Related Code:** [PR #224](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/224)  
**Related Issue:** [#211](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/211)

---

### Challenges

#### Challenge 1: Establishing Clean Architecture Foundation for Android Application
**Description:** Setting up the initial Android application architecture with Clean Architecture principles, MVVM pattern, dependency injection, and navigation system required careful planning and integration of multiple Android libraries and frameworks.

**Context:**
- Related Feature: Mobile Application Initialization
- Related Issue: [#16](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/16)
- Related Code: [PR #21](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/21)

**Problem:** The main challenge was establishing a scalable architecture from scratch that would support the entire mobile application development lifecycle. This involved: (1) Deciding on appropriate layer separation (data, domain, presentation) and ensuring proper dependency direction, (2) Integrating multiple Android libraries (Navigation Component, Hilt for dependency injection, Retrofit for networking) without creating circular dependencies, (3) Setting up navigation with nested graphs while maintaining proper back stack management, (4) Implementing ViewState pattern for state management that works seamlessly with Android's lifecycle, and (5) Creating a foundation that other team members could easily understand and extend.

**Solution:** I addressed this by: (1) Creating a clear module structure separating data, domain, and presentation layers with explicit dependency rules, (2) Using Hilt for dependency injection with proper scoping (Application, Activity, ViewModel scopes) to manage object lifecycles correctly, (3) Implementing a ViewState sealed class pattern that encapsulates all possible UI states (Loading, Success, Error) using Kotlin's sealed classes for type safety, (4) Setting up Navigation Component with nested graphs for bottom navigation tabs, ensuring each tab has its own navigation graph to prevent back stack conflicts, (5) Creating comprehensive documentation in `mobile/README.md` explaining the architecture, design patterns, and development guidelines for team members.

**Lessons Learned:** I learned that investing time upfront in establishing a solid architectural foundation pays significant dividends throughout the project lifecycle. The clear separation of concerns made it easier for team members to contribute, and the ViewState pattern eliminated many common Android lifecycle-related bugs. Additionally, comprehensive documentation is crucial for team collaboration, especially when working with complex architectural patterns. The modular approach also made testing much easier, as each layer could be tested independently.

#### Challenge 2: Complex Permission Logic for Report System with Role-Based Access
**Description:** Implementing the reporting system required designing complex permission logic where different user roles (Admins, Moderators, regular users) have different access levels to reports, with moderators only seeing reports for spaces they moderate, while admins see all reports.

**Context:**
- Related Feature: Report functionality
- Related Issue: [#118](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/118)
- Related Code: [PR #123](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/123)

**Problem:** The main challenges were: (1) Designing permission logic that correctly filters reports based on user roles - Admins see all reports, Moderators only see reports for spaces they moderate, and regular users only see reports they created, (2) Ensuring space creators are automatically added as moderators when a space is created, requiring coordination with space creation logic, (3) Implementing report aggregation to count multiple reports for the same entity while maintaining individual report details, (4) Handling edge cases such as preventing users from reporting their own content, users with multiple roles, and spaces with no moderators, (5) Ensuring the frontend receives the correct data structure including `report_count` for displaying aggregated report information, (6) Testing permission logic thoroughly to ensure security and prevent unauthorized access.

**Solution:** I addressed this by: (1) Creating custom permission classes in Django REST Framework that check user roles and space moderation relationships, implementing separate permission classes for list views (filtered by role) and detail views, (2) Overriding the `get_queryset()` method in the ReportViewSet to apply role-based filtering at the database level, ensuring efficient queries and preventing data leakage, (3) Implementing report aggregation logic in the serializer that groups reports by content type and content ID, calculating `report_count` for each unique reported entity, (4) Adding validation in the serializer to prevent users from reporting their own discussions, (5) Coordinating with space creation logic to ensure space creators are automatically assigned moderator roles, (6) Creating comprehensive unit tests covering all permission scenarios, (7) Adding the missing `report_count` parameter to the GET response after code review feedback.

**Lessons Learned:** I learned that permission logic is one of the most critical aspects of a reporting/moderation system, and security should never be compromised. Implementing role-based access control requires careful consideration of all user roles and their relationships. Testing permission logic thoroughly is essential, as security vulnerabilities can have serious consequences. The experience also taught me the importance of coordinating with other features to ensure data consistency, and that code reviews are invaluable for catching missing parameters or edge cases that might not be obvious during initial implementation.

---

### Code Review

#### Code Reviews Provided

As a project maintainer, I actively reviewed and merged 26 pull requests from team members, ensuring code quality, consistency, and adherence to project standards. Below are notable examples:

1. **Review for Gamze Güneri**
   - **Code Reviewed:** Various frontend features and improvements including styling, graph interactions, UI enhancements, and archive functionality
   - **Pull Request:** Multiple PRs including [PR #158](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/158), [PR #226](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/226), [PR #235](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/235), [PR #240](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/240)
   - **Code Review Link:** [PR #158](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/158), [PR #226](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/226), [PR #235](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/235), [PR #240](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/240)
   - **Conversation:** [PR Conversations](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pulls?q=is%3Apr+author%3Apinacotheque)
   
   **Review Interaction:**
   I reviewed multiple PRs from Gamze covering frontend improvements such as full-screen graph view, styling enhancements for login/register pages, filter/search functionality for the back office, and archive functionality. For the archive functionality PR (#158), I tested the implementation as an admin and moderator but encountered 403 Forbidden errors when trying to archive items or list archived items. I raised concerns about the user role assignment process, questioning whether the functionality should work out of the box without requiring shell commands to assign admin roles, as this approach wouldn't be feasible in a deployed application. I also questioned whether moderators and space creators should be able to archive and list archived items, and whether user role assignment should require shell commands. I suggested that additional testing by other team members would be beneficial to identify if the issue was specific to my setup or a broader problem.
   
   **Resulting Changes:**
   The review process identified potential issues with permission handling and user role assignment in the archive functionality. My testing and feedback highlighted the importance of ensuring the feature works correctly with proper user roles without requiring manual shell commands, which is critical for production deployment. The PR was merged after addressing the concerns, and the archive functionality was successfully integrated into the application.

2. **Review for Yasemin Tangül**
   - **Code Reviewed:** Backend features including property and property value logic, graph database integration, and infrastructure updates
   - **Pull Request:** Multiple PRs including [PR #243](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/243), [PR #244](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/244), [PR #245](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/245)
   - **Code Review Link:** [PR #243](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/243), [PR #244](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/244), [PR #245](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/245)
   - **Conversation:** [PR Conversations](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pulls?q=is%3Apr+author%3Aytangul1)
   
   **Review Interaction:**
   I reviewed Yasemin's backend implementations focusing on property management, graph database integration, and infrastructure configurations. Reviews emphasized proper API design, database migration handling, test coverage, and alignment with the project's backend architecture.
   
   **Resulting Changes:**
   Reviewed PRs were merged after verification of proper implementation, test coverage, and adherence to backend standards. These contributions enhanced the backend functionality and infrastructure.

3. **Review for Batuhan Cömert**
   - **Code Reviewed:** Interactive activity stream feature implementation
   - **Pull Request:** [PR #229](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/229) - Feature interactive activity stream
   - **Code Review Link:** [PR #229](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/229)
   - **Conversation:** [PR #229 Conversation](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/229)
   
   **Review Interaction:**
   I reviewed Batuhan's implementation of the interactive activity stream feature. The review focused on ensuring proper data flow, component structure, user interaction patterns, and integration with the existing frontend architecture.
   
   **Resulting Changes:**
   After review and approval, the PR was merged. The activity stream feature was successfully integrated into the application, providing users with an interactive way to view and engage with activities.

#### Code Reviews Received

I received valuable code reviews from team members on my pull requests, which helped improve code quality and maintain consistency across the project.

1. **Review from Team Members (Esra Nur Özüm)**
   - **Pull Request:** [PR #38](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/38) - User profile screen with unit tests for mobile application
   - **Code Review Link:** [PR #38](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/38)
   - **Conversation:** [PR #38 Conversation](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/38)
   
   **Nature of Review:**
   Esra reviewed my implementation of the user profile screen for the Android application and identified several UI/UX consistency issues and validation problems. The feedback included: (1) Navigation issue - inability to navigate back from Space page to Profile page using the bottom navigation bar, and the profile tab not functioning when clicked again, (2) UI consistency issues - button color inconsistency (black vs blue), text box border colors differing from Login/Register screens, and shadow disappearing under bottom navigation bar when navigating to space page, (3) Input validation issues - Profession field could be left empty, no character limit for Profession field, and missing 500-character limit enforcement for Bio field (database constraint), (4) Accessibility issue - arrows in space cards disappearing when device font size is increased.
   
   **How Addressed:**
   I addressed the feedback by implementing the following fixes: (1) Fixed button color consistency throughout the application to align with the design system, (2) Updated text box border colors to match those used in Login and Register screens for consistency, (3) Added input validation to ensure the Profession field cannot be left blank, (4) Implemented character limit for the Profession field to prevent unlimited text input, (5) Enforced the 500-character limit for the Bio field to match the database constraint, (6) Fixed the font size accessibility issue by ensuring arrows in space cards remain visible when device font size is increased. For the navigation issues (bottom navigation bar behavior and profile tab navigation), I acknowledged these as app-wide problems and suggested creating a separate bugfix ticket since they were not specific to this PR. The PR was updated with these fixes and merged successfully.

2. **Review from Team Members (Gamze Güneri)**
   - **Pull Request:** [PR #123](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/123) - Report functionality on backend
   - **Code Review Link:** [PR #123](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/123)
   - **Conversation:** [PR #123 Conversation](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/123)
   
   **Nature of Review:**
   Gamze reviewed my comprehensive reporting system implementation which included backend models, serializers, views, and frontend integration. The implementation featured report endpoints (POST/GET/PATCH /api/reports/, GET /api/reports/reasons/), moderator specialization (Admins see all reports, Moderators see only reports for spaces they moderate), frontend ReportModal integration, and fixes for preventing users from reporting their own discussions and ensuring space creators are added as moderators. Gamze identified that the `report_count` parameter was missing from the GET response, which was needed for the frontend to display the number of reports for each entity.
   
   **How Addressed:**
   I immediately addressed the feedback by adding the `report_count` parameter to the GET response in a follow-up commit. This ensured that the frontend could properly display the number of reports aggregated for each reported entity, which was essential for the back-office reporting interface. After the fix, Gamze confirmed that the implementation looked good, and the PR was merged successfully.


3. **Review from Team Members (Gamze Güneri)**
   - **Pull Request:** [PR #151](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/151) - Dismiss report functionality
   - **Code Review Link:** [PR #151](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/151)
   - **Conversation:** [PR #151 Conversation](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/151)
   
   **Nature of Review:**
   Gamze reviewed my implementation of the dismiss report functionality, which added the DISMISSED status to the reporting system. She raised two important concerns: (1) Whether DISMISSED items should be displayed in the report list, as this could clutter the interface with many dismissed records, and (2) The purpose and necessity of the DISMISSED status in the current workflow, questioning if it serves a clear function at this stage of development.
   
   **How Addressed:**
   I explained that the DISMISSED status was implemented based on class discussions about allowing other admins or moderators to review the results of handled reports later, providing transparency and accountability in the moderation process. I acknowledged that there might be other aspects of the reporting functionality that need refinement, as it is a comprehensive feature. I suggested that we discuss further improvements and feedback after testing in a team huddle to make collaborative decisions on enhancements. This approach allowed for iterative improvement of the feature based on real-world usage and team feedback.

---

### Pull Requests

The following table lists the major pull requests that I have contributed and were accepted:

| PR # | Title | Description | Related Issues | URL |
|------|------|-------------|----------------|-----|
| #21 | Initial Android project setup | Initialize Android app with MVVM, bottom bar, navigation components and sample screens | [#16](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/16) | [PR #21](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/21) |
| #38 | User profile screen with unit tests for mobile application | Implement profile screen with edit functionality, including comprehensive unit tests | [#28](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/28) | [PR #38](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/38) |
| #63 | Connect login, register & profile screens to backend Android | Add backend services, connect login, register & profile to API, implement auth token persist logic | [#58](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/58) | [PR #63](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/63) |
| #89 | Property names should be displayed at node creation and node details | Improve property selection & edit in node creation & edit flow using SPARQL query, add search functionality | [#86](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/86) | [PR #89](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/89) |
| #151 | Dismiss report functionality | In current state, backend supports reporting and displaying content. Report dismiss functionality should also be added to backend which will be used by admins or moderators via backoffice | [#149](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/149) | [PR #151](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/151) |
| #102 | Edge format with Wikidata properties | Currently edges are free form text. We should support them with wikidata properties when possible by search. If there is no properties that user desires to use, then user should be able to add free form text | [#85](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/85) | [PR #102](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/102) |
| #117 | Language support foundation for web | Add support for string resources for web frontend & add translations for Turkish, add specific error descriptions | [#87](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/87) | [PR #117](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/117) |
| #123 | Report functionality on backend | Add reporting functionality with associated models, serializers, and views & connect report functionality with web frontend | [#118](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/118) | [PR #123](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/123) |
| #211 | Improve Space Graph | Updated SpaceGraph and SpaceDetails components to support instance type filtering and display | [#211](github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/211) | [PR #224](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/224) |
| #179 | Grouping the values for same properties on node card | To improve the UI/UX of the node details, the properties with same label should be grouped | [#179](github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/179) | [PR #190](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/190) |

---

### Issues

#### Issues Created

The following table lists the major issues that I created related to this project:

| Issue # | Title | URL |
|---------|-------|-----|
| #16 | Initial Mobile Application will be Started | [Issue #16](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/16) |
| #28 | User profile screen with unit tests for mobile application | [Issue #28](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/28) |
| #189 | AI Summary functionality for space details | [Issue #189](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/189) |
| #85 | Edge format with Wikidata properties | [Issue #85](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/85) |
| #86 | Property names should be displayed at node creation and node details | [Issue #86](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/86) |
| #87 | Language support foundation for web | [Issue #87](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/87) |
| #186| Add image to node details on web | [Issue #186](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/186) |
| #118 | Report functionality on backend | [Issue #118](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/118) |
| #179 | Grouping the values for same properties on node card| [Issue #179](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/179) |
| #211 | Node display according to instance types & quick filtering | [Issue #211](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/211) |

--- 

### HONOR CODE

Related to the submission of all the project deliverables for the Swe574 Fall 2025
semester project reported in this report, I Mehmet Yusuf Bayam declare that:
- I am a student in the Software Engineering MS program at Bogazici University and
am registered for Swe574 course during the Fall 2025 semester.
- All the material that I am submitting related to my project (including but not lim-
ited to the project repository, the final project report, and supplementary documents)
have been exclusively prepared by myself.
- I have prepared this material individually without the assistance of anyone else
with the exception of permitted peer assistance, which I have explicitly disclosed in
this report.

**Name:** Mehmet Yusuf Bayam

**Signature** 
<img width="260" height="100" alt="resim" src="https://github.com/user-attachments/assets/35f552c1-4b0b-42e6-a489-137b94ca617d" />


# Individual Contributions - Gamze

## Student Information

**Name:** Gamze Güneri

**Student ID:** 2023719159

**Project:** Connect The Dots

**Semester:** Fall 2025

## Executive Summary

This report documents my individual contributions to the Connect The Dots project, focusing primarily on community management infrastructure, back-office system design, AI-assisted features, advanced discovery mechanisms and overall product quality. Throughout the project, I contributed as a product owner, frontend and backend developer, system designer and active reviewer.

My most significant contribution was the conception, design and implementation of the Backoffice (Admin Dashboard), which enables moderation, analytics, reporting and governance for CTD communities. In addition, I proposed and co-developed the AI-powered Space Summary feature, contributed to advanced search and graph interaction improvements and actively participated in code review and quality assurance across the project lifecycle.

Rather than working in isolation within a single module, my contributions supported the project flow by shaping product direction, implementing critical features, validating system behavior and preparing the platform for final delivery.

---

## Demo

### Back-Office Administration Module

Description:
The back-office is a dedicated administration area designed to support platform moderation and management. It allows administrators and moderators to view platform analytics, manage users, review reports and handle archived content in a structured way. I designed and implemented this module from scratch, including its overall structure, navigation flow and individual pages. Throughout the process, I took on the roles of product owner, designer and developer, shaping both how the system works and how it feels to use.

Back Office Overview
<img alt="Screenshot 2025-12-20 at 23 35 51" src="https://github.com/user-attachments/assets/8aa7e294-6ade-47ab-91ac-ca71678bd670" />


Related Code:

BackOffice.jsx
Analytics.jsx
Overview.jsx
Users.jsx

Related Issues: #48, #99, #200

### AI-Powered Space Summary

Description:
The AI-powered summarization feature helps users quickly understand complex spaces by generating a structured summary based on nodes, connections, discussions and user contributions. I worked on both the backend and frontend aspects of this feature, including API integration, loading and error states and UI presentation. The implementation also respects system rules, such as disabling summarization for archived spaces and includes unit tests to ensure reliability.


AI Summary Output
<img alt="Screenshot 2025-12-20 at 23 36 26" src="https://github.com/user-attachments/assets/ba9abe0f-c367-4b38-8647-1a2b440c49c6" />



Related Code:
Frontend: SpaceDetails.jsx (AI Summary UI)
Backend: views.py (AI endpoint)

Related Issues: #189, #220, #222

---
## Challenges

### Challenge 1: Building the Archive Feature End-to-End

Implementing the archive feature was challenging because it required both backend and frontend work, along with careful permission handling. The system needed to support different content types such as spaces, nodes and discussions, while also respecting the differences between moderator and admin roles.

At first, some permission logic was inconsistent and could have caused unauthorized actions. I fixed this by clearly separating moderator and admin permissions and making sure API responses followed a consistent structure so frontend could integrate smoothly. I also added tests to cover edge cases.

This experience taught me how important it is to think about permissions early on and how valuable code reviews are for catching issues that are easy to miss when working alone.



### Challenge 2: Creating the Back-Office Without Clear Initial Requirements

The back-office module was not part of the original plan, so I had to define its purpose and structure from scratch. I needed to think ahead about moderation, analytics and user management while keeping the system simple and adaptable.

I started with a minimal structure focused on core needs and used a tab-based layout so new features could be added easily later. Using mock data early on helped me move forward without waiting for backend APIs and allowed the team to give feedback quickly.

From this process, I learned that starting from zero requires iteration, patience and clear ownership. Building something step by step and improving it with feedback is often more effective than trying to design everything perfectly from the beginning.

---
## Major Personal Efforts

#### Major Personal Effort 1: Back-Office Administration Module

**Related Requirement:**  
Administrative dashboard and content management

**Description:**  
One of my most significant contributions to the project was the design and development of the Back-Office Administration Module, which I built from scratch. I introduced the idea of having a dedicated back-office early in the project, recognizing that a large, collaborative community platform would require strong moderation, analytics and management capabilities to scale sustainably.

I designed the overall workflows, page hierarchy and user interactions for the admin dashboard and implemented the frontend components that allow administrators to monitor platform activity through analytics dashboards, manage users, review reports and control archived content. The back-office includes tab-based navigation, responsive layouts and mock data support to enable testing and iteration during early development stages.

Throughout this effort, I acted as the product owner, designer and developer, translating abstract needs into concrete features and ensuring the back-office remained aligned with both technical constraints and product vision.

**Related Issues:**  
- #48 – Back-office frontend initialization  
- #99 – Admin dashboard analytics page  
- #200 – User management list  

**Relevant URLs:**  
- BackOffice.jsx  
- Analytics.jsx  
- Users.jsx  
- Overview.jsx  
- Header.jsx (Admin navigation)

**Outcome:**  
A fully functional administration system that enables effective community management and plays a central role in maintaining platform quality, safety and scalability.

---

#### Major Personal Effort 2: Archive Functionality with Backend Integration

**Related Requirement:**  
Content lifecycle management and moderation

**Description:**  
I implemented the **archive functionality** as a full-stack feature, enabling moderators and administrators to archive and restore spaces, nodes and discussions. This design allows content to be temporarily removed from active participation while preserving historical data and relationships.

On the backend, I contributed to database models, API endpoints, serializers and permission logic. On the frontend, I designed and implemented the archive management interface within the back-office, including filtering, search and restore actions. Role-based access control ensures that only authorized users can manage archived content.

**Related Issues:**  
- #127 – Archive module with backend & frontend implementation  

**Relevant URLs:**  
- Backend: `models.py`, `views.py`, `permissions.py`, `serializers.py`  
- Frontend: `Archive.jsx`

**Outcome:**  
A robust archive system that strengthens moderation workflows and preserves long-term data integrity.

---

#### Major Personal Effort 3: Content Reporting System

**Related Requirement:**  
Community safety and content moderation

**Description:**  
I designed and implemented the **content reporting system**, enabling users to report inappropriate spaces, nodes, discussions, or users. The system includes a reusable reporting modal where users can select predefined reasons and optionally provide additional context.

On the administrative side, I developed interfaces that allow moderators to review incoming reports, evaluate user history and take appropriate actions through the back-office. The reporting system also supports multilingual usage through integration with the platform’s localization infrastructure.

**Related Issues:**  
- #115 – Report feature UI for users, spaces, nodes and comments  
- #126 – Display report list in admin dashboard with action buttons  

**Relevant URLs:**  
- `ReportModal.jsx`  
- `Reports.jsx`  
- `SpaceDetails.jsx` 
- `NodeDetailModal.jsx`
- `Profile.jsx`

**Outcome:**  
A complete reporting and moderation pipeline that improves platform trust and community safety.

---

#### Major Personal Effort 4: Advanced Search Functionality

**Related Requirement:**  
Enhanced discovery and information retrieval

**Description:**  
I developed the **advanced search functionality** within spaces to help users explore large and complex graphs more effectively. The feature supports general text-based search as well as property-based filtering, allowing users to narrow results by node properties, edge properties, tags and Wikidata-related attributes.

I designed the search interface with collapsible panels, clear visual feedback and backend API integration to ensure accurate and responsive results, even in spaces with a high number of nodes.

**Related Issues:**  
- #180 – Advanced search UI and backend integration  

**Relevant URLs:**  
- `SpaceDetails.jsx`

**Outcome:**  
An effective discovery mechanism that improves navigation and understanding within complex knowledge spaces.

---

#### Major Personal Effort 5: AI-Powered Space Summarization

**Related Requirement:**  
Content understanding and onboarding support

**Description:**  
I proposed and co-developed the **AI-powered space summarization feature**, which generates structured summaries based on nodes, edges, discussions and user contributions within a space. This feature is designed to help users quickly grasp the context and key insights of a space, particularly during onboarding.

I contributed to both backend and frontend implementation, including API integration, loading states, error handling and UI presentation. The feature respects system states such as archived spaces and includes unit tests to validate functionality and edge cases.

**Related Issues:**  
- #189 – AI summary implementation  
- #220 – Disable summarize button for archived spaces  
- #222 – Unit tests for archive and report features  

**Relevant URLs:**  
- Backend: AI summarization endpoint
- Frontend: `SpaceDetails.jsx`  
- Tests: `test_archive.py`  

**Outcome:**  
An AI-assisted feature that significantly improves onboarding, comprehension and overall usability of the platform.

---

#### Major Personal Effort 6: Information Modals and UX Refinement

**Related Requirement:**  
User onboarding and interaction clarity

**Description:**  
I designed and implemented **information modals** across the application to explain how key space actions work, such as creating content, reporting, archiving and interacting with the graph. These modals provide contextual guidance and help new users understand system behavior without disrupting their workflow.

In addition to information modals, I actively contributed to broader UX refinements across the application. This included improving layout consistency, refining interaction feedback and ensuring visual clarity across complex views like graphs and dashboards.

During development, I consistently used **localization (i18n) translations** and **centralized color variables** to dynamically enhance the system. This approach ensured language flexibility, visual consistency, accessibility compliance and easier future maintenance.

**Relevant URLs:**  
- Information modal components  
- Localization files (`i18n`)  
- Theme and color variables

**Outcome:**  
Improved onboarding experience, clearer user interactions and a more consistent, accessible and maintainable UI across the entire platform.


## Documentation

### WebMockups Page
- **Wiki Page:** [WebMockups](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/WebMockups)

I created and maintained the WebMockups wiki page where I documented the visual designs and mockups for web. This included:
- **Back Office Initial Designs:** Added screenshots of the admin dashboard, analytics page, user management and reports interface that I designed and implemented
- **Reporting Feature:** Documented the UI designs for reporting spaces, nodes and discussion comments with screenshots showing the modal designs
- **Activity Stream:** Added mockups for the activity stream feature
- **Space Specific Analytics:** Documented the analytics dashboard designs showing metrics and visualizations

This page served as a reference for the team during development and helped maintain design consistency across the back-office features.

---

### Requirements Specification
- **Wiki Page:** [RequirementsSpecification](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/RequirementsSpecification)

I contributed to the Requirements Specification with some modifications, particularly around the back-office administration features, content moderation workflows and reporting system requirements. Helped refine the functional requirements to ensure they accurately reflected what we needed to build.

---

### Development Workflow Page
- **Wiki Page:** [DevelopmentWorkflow](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/wiki/DevelopmentWorkflow)

I contributed to the development workflow documentation, helping establish and document our team's development practices. This included guidelines for branching strategies, PR processes and how we coordinate between frontend and backend development.


## Code Review

### Code Reviews Provided

#### 1. Review for Mehmet Yusuf Bayam - Report Functionality Backend
- **Pull Request:** [PR #123](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/123) | **Issue:** [#118](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/118)

I reviewed the backend reporting API since I was building the admin dashboard. Suggested adding more metadata to serializers (report_count) for better display, improving permission checks to distinguish user roles, grouping reports by content and better status validation. Yusuf updated the code accordingly and it integrated well with my frontend.

---

#### 2. Review for Mehmet Yusuf Bayam - Language Support Foundation
- **Pull Request:** [PR #117](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/117) | **Issue:** [#87](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/87)

Reviewed the i18n implementation. Suggested organizing translation keys by feature, adding a language switcher in the header, fixing remaining hardcoded strings, using namespaces and adding fallback text. Yusuf restructured the files and added all the improvements.

---

#### 3. Review for Mehmet Yusuf Bayam - Dismiss Report and Delete Discussion
- **Pull Request:** [PR #151](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/151) | **Issues:** [#148](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/148), [#149](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/149)

Reviewed admin moderation features. Suggested confirmation dialogs, reason fields for dismissals, soft deletion with messages, undo functionality and activity logs. Yusuf added confirmations, soft deletion and audit logging. Made the moderation workflow safer.

---

### Code Reviews Received

#### 1. Review from Mehmet Yusuf Bayam - Archive Module Implementation
- **Pull Request:** [PR #158](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/158) | **Issue:** [#127](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/127)

Yusuf reviewed my archive module implementation that I built for spaces, nodes and discussions. He caught several issues with permission logic that needed refinement for moderator access and inconsistent API response formats. I improved permission checks, standardized responses and added better test coverage.

---

#### 2. Review from Team - Information Box for Space View
- **Pull Request:** [PR #195](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/195) | **Issue:** [#192](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/192)

The team reviewed my information modal component that provides contextual help in the space view. Received feedback on improving the modal's localization texts. I updated the component with proper translation labels and improved the styling for better visibility.

---

#### Summary

I actively participated in code reviews throughout the project, providing feedback for backend and frontend features while receiving valuable reviews. The review process helped catch bugs early and improve code quality across the team.


## Pull Requests

| PR # | Issue | Title | Link |
|------|-------|-------|------|
| #70 | #48 | Initialize back office frontend | [PR #70](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/70) |
| #94 | #93 | Fix space card clickable & home page lint error | [PR #94](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/94) |
| #98 | #96 | Fix 400 error while creating space with existing tag | [PR #98](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/98) |
| #100 | #99 | Admin dashboard analytics page | [PR #100](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/100) |
| #111 | #108 | Add useClickOutside hook to reuse cancel modal function | [PR #111](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/111) |
| #116 | #115 | Add report feature UI for users, spaces, nodes and comments | [PR #116](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/116) |
| #152 | #126 | Add display report list in admin dashboard and action button | [PR #152](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/152) |
| #158 | #127 | Add archive module with backend & frontend implementation | [PR #158](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/158) |
| #182 | #180 | Advanced search UI and backend integration | [PR #182](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/182) |
| #193 | #191 | Refactor edge details modal with more informative UI | [PR #193](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/193) |
| #195 | #192 | Add information modal to space view | [PR #195](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/195) |
| #197 | #196 | Fix dashboard top contributed spaces width truncated | [PR #197](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/197) |
| #201 | #200 | Add user management list in back-office | [PR #201](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/201) |
| #204 | #203 | Fix space with many tags overflows the card view | [PR #204](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/204) |
| #210 | #189 | Add AI summary implementation | [PR #210](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/210) |
| #215 | #212 | Space graph layout improvements | [PR #215](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/215) |
| #218 | #216 | Add top contributors list to collaborator dropdown | [PR #218](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/218) |
| #221 | #220 | Fix disabling the summarize button when space is archived | [PR #221](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/221) |
| #223 | #222 | Add archive and report pages improvements and unit tests | [PR #223](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/223) |
| #226 | #225 | Add full screen space graph view | [PR #226](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/226) |
| #235 | #233 | Add logo, login/register and overall styling improvements | [PR #235](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/235) |
| #238 | #237 | Fix edge property search dropdown cancel | [PR #238](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/238) |
| #240 | #239 | Add filter and search functionality to backoffice | [PR #240](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/240) |
| #241 | #236 | Add outside click hook to edge detail modal | [PR #241](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/pull/241) |


## Issues Created

| Issue # | Title | Related PR | Link |
|---------|-------|------------|------|
| #48 | Back-office frontend initialization | #70 | [Issue #48](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/48) |
| #93 | Fix space card clickable & home page lint error | #94 | [Issue #93](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/93) |
| #96 | Fix 400 error while creating space with existing tag | #98 | [Issue #96](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/96) |
| #99 | Admin dashboard analytics page | #100 | [Issue #99](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/99) |
| #108 | Add useClickOutside hook to reuse cancel modal function | #111 | [Issue #108](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/108) |
| #115 | Add report feature UI for users, spaces, nodes and comments | #116 | [Issue #115](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/115) |
| #126 | Add display report list in admin dashboard and action button | #152 | [Issue #126](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/126) |
| #127 | Add archive module with backend & frontend implementation | #158 | [Issue #127](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/127) |
| #180 | Advanced search UI and backend integration | #182 | [Issue #180](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/180) |
| #191 | Refactor edge details modal with more informative UI | #193 | [Issue #191](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/191) |
| #192 | Add information modal to space view | #195 | [Issue #192](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/192) |
| #196 | Fix dashboard top contributed spaces width truncated | #197 | [Issue #196](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/196) |
| #200 | Add user management list in back-office | #201 | [Issue #200](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/200) |
| #203 | Fix space with many tags overflows the card view | #204 | [Issue #203](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/203) |
| #212 | Space graph layout improvements | #215 | [Issue #212](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/212) |
| #216 | Add top contributors list to collaborator dropdown | #218 | [Issue #216](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/216) |
| #220 | Fix disabling the summarize button when space is archived | #221 | [Issue #220](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/220) |
| #222 | Add archive and report pages improvements and unit tests | #223 | [Issue #222](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/222) |
| #225 | Add full screen space graph view | #226 | [Issue #225](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/225) |
| #233 | Add logo, login/register and overall styling improvements | #235 | [Issue #233](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/233) |
| #236 | Add outside click hook to edge detail modal | #241 | [Issue #236](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/236) |
| #237 | Fix edge property search dropdown cancel | #238 | [Issue #237](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/237) |
| #239 | Add filter and search functionality to backoffice | #240 | [Issue #239](https://github.com/SWE574-Connect-The-Dots/SWE574-ConnectTheDots/issues/239) |



## HONOR CODE
Related to the submission of all the project deliverables for the SWE574 Fall 2025 semester project reported in this report, I Gamze Güneri declare that:
- I am a student in the Software Engineering MS program at Bogazici University and am registered for SWE574 course during the Fall 2025 semester.
- All the material that I am submitting related to my project (including but not limited to the project repository, the final project report and supplementary documents) have been exclusively prepared by myself.
- I have prepared this material individually without the assistance of anyone else with the exception of permitted peer assistance, which I have explicitly disclosed in this report.

**Name:** Gamze Güneri

**Signature** 
<img width="310" height="150" alt="resim" src="https://github.com/user-attachments/assets/535d88ae-5ee1-4033-a1c9-85f2922806a8" />


