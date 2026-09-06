#!/usr/bin/env python3
"""
ECLMS Technical Report — PDF Generator
Generates a professionally styled PDF with visual diagrams, colored tables,
and proper typographic formatting.
"""

from fpdf import FPDF
import os
import textwrap

# ── Colour Palette ─────────────────────────────────────────────────────
NAVY      = (15, 23, 42)       # Dark headers / titles
BLUE      = (37, 99, 235)      # Section headings / accents
TEAL      = (20, 184, 166)     # Subsection headings
DARK_GRAY = (51, 65, 85)       # Body text
LIGHT_BG  = (241, 245, 249)    # Table stripe / callout backgrounds
WHITE     = (255, 255, 255)
TBL_HEAD  = (30, 58, 138)      # Table header blue
TBL_HEAD2 = (88, 28, 135)      # Table header purple (alternate)
GREEN_ACC = (21, 128, 61)      # Success / annotation colour
ORANGE    = (234, 88, 12)      # Warning colour
RED_ACC   = (185, 28, 28)      # Error colour
BORDER    = (203, 213, 225)    # Light border

# ── Reusable PDF subclass ──────────────────────────────────────────────

class ReportPDF(FPDF):
    MARGIN = 18

    def __init__(self):
        super().__init__(orientation='P', unit='mm', format='A4')
        self.set_auto_page_break(auto=True, margin=20)
        self.set_margins(self.MARGIN, self.MARGIN, self.MARGIN)
        self.alias_nb_pages()

        # Register fonts
        # We'll use Helvetica (built-in) as primary
        self.body_w = 210 - 2 * self.MARGIN  # usable width

    # ── Header / Footer ────────────────────────────────────────────────
    def header(self):
        if self.page_no() == 1:
            return  # Cover page has its own header
        self.set_font('Helvetica', 'I', 8)
        self.set_text_color(*DARK_GRAY)
        self.cell(0, 6, 'ECLMS — Enterprise Contract Lifecycle Management System  |  Technical Report', align='L')
        self.ln(2)
        self.set_draw_color(*BLUE)
        self.set_line_width(0.5)
        self.line(self.MARGIN, self.get_y(), 210 - self.MARGIN, self.get_y())
        self.ln(6)

    def footer(self):
        self.set_y(-15)
        self.set_font('Helvetica', 'I', 8)
        self.set_text_color(*DARK_GRAY)
        if self.page_no() > 1:
            self.cell(0, 10, f'Page {self.page_no()}/{{nb}}', align='C')

    # ── Utility: coloured rectangle behind text ────────────────────────
    def colour_rect(self, x, y, w, h, colour):
        self.set_fill_color(*colour)
        self.rect(x, y, w, h, 'F')

    # ── Utility: Section heading (H1 / H2 / H3) ──────────────────────
    def section_h1(self, text, num=''):
        self.ln(4)
        y0 = self.get_y()
        self.colour_rect(self.MARGIN, y0, self.body_w, 12, NAVY)
        self.set_xy(self.MARGIN + 4, y0 + 2)
        self.set_font('Helvetica', 'B', 14)
        self.set_text_color(*WHITE)
        display = f'{num}  {text}' if num else text
        self.cell(self.body_w - 8, 8, display)
        self.ln(14)

    def section_h2(self, text, num=''):
        self.ln(3)
        y0 = self.get_y()
        self.colour_rect(self.MARGIN, y0, self.body_w, 10, BLUE)
        self.set_xy(self.MARGIN + 4, y0 + 1.5)
        self.set_font('Helvetica', 'B', 11)
        self.set_text_color(*WHITE)
        display = f'{num}  {text}' if num else text
        self.cell(self.body_w - 8, 7, display)
        self.ln(12)

    def section_h3(self, text, num=''):
        self.ln(2)
        self.set_font('Helvetica', 'B', 10)
        self.set_text_color(*TEAL)
        prefix = f'{num}  ' if num else ''
        self.cell(0, 6, f'{prefix}{text}')
        self.ln(4)
        # underline
        self.set_draw_color(*TEAL)
        self.set_line_width(0.3)
        self.line(self.MARGIN, self.get_y(), self.MARGIN + min(len(text) * 2.5, self.body_w), self.get_y())
        self.ln(3)

    # ── Utility: Body text ─────────────────────────────────────────────
    def body_text(self, text, bold=False):
        style = 'B' if bold else ''
        self.set_font('Helvetica', style, 9.5)
        self.set_text_color(*DARK_GRAY)
        self.multi_cell(self.body_w, 5, text)
        self.ln(1)

    def bullet(self, text, indent=6):
        x0 = self.get_x()
        self.set_x(self.MARGIN + indent)
        self.set_font('Helvetica', '', 9.5)
        self.set_text_color(*DARK_GRAY)
        self.cell(4, 5, chr(0x2022))  # bullet char
        self.multi_cell(self.body_w - indent - 4, 5, text)
        self.ln(0.5)

    def code_block(self, text, width=None):
        w = width or self.body_w
        self.ln(1)
        lines = text.split('\n')
        y0 = self.get_y()
        h = len(lines) * 4.2 + 4
        if y0 + h > 280:
            self.add_page()
            y0 = self.get_y()
        self.colour_rect(self.MARGIN + 2, y0, w - 4, h, (30, 41, 59))
        self.set_xy(self.MARGIN + 5, y0 + 2)
        self.set_font('Courier', '', 7.5)
        self.set_text_color(226, 232, 240)
        for line in lines:
            self.cell(w - 10, 4.2, line[:120])
            self.ln(4.2)
            self.set_x(self.MARGIN + 5)
        self.set_text_color(*DARK_GRAY)
        self.ln(3)

    def info_box(self, title, text, colour=BLUE):
        self.ln(2)
        y0 = self.get_y()
        total_h = 14 + (len(text) // 90) * 5
        if y0 + total_h > 280:
            self.add_page()
            y0 = self.get_y()
        # left accent bar
        self.set_fill_color(*colour)
        self.rect(self.MARGIN, y0, 3, total_h, 'F')
        # background
        self.set_fill_color(*(c + (255 - c) * 85 // 100 for c in colour))
        self.rect(self.MARGIN + 3, y0, self.body_w - 3, total_h, 'F')
        self.set_xy(self.MARGIN + 7, y0 + 2)
        self.set_font('Helvetica', 'B', 9)
        self.set_text_color(*colour)
        self.cell(0, 5, title)
        self.ln(6)
        self.set_x(self.MARGIN + 7)
        self.set_font('Helvetica', '', 8.5)
        self.set_text_color(*DARK_GRAY)
        self.multi_cell(self.body_w - 12, 4.5, text)
        self.ln(3)

    # ── Utility: Styled table ──────────────────────────────────────────
    def styled_table(self, headers, rows, col_widths=None, header_colour=TBL_HEAD):
        if col_widths is None:
            col_widths = [self.body_w / len(headers)] * len(headers)
        # Ensure widths don't exceed body_w
        total = sum(col_widths)
        if total > self.body_w:
            factor = self.body_w / total
            col_widths = [w * factor for w in col_widths]

        # Header row
        self.set_fill_color(*header_colour)
        self.set_text_color(*WHITE)
        self.set_font('Helvetica', 'B', 8)
        row_h = 7
        for i, h in enumerate(headers):
            self.cell(col_widths[i], row_h, f' {h}', border=1, fill=True)
        self.ln(row_h)

        # Data rows
        self.set_font('Helvetica', '', 7.5)
        for row_idx, row in enumerate(rows):
            stripe = row_idx % 2 == 0
            if stripe:
                self.set_fill_color(*LIGHT_BG)
            else:
                self.set_fill_color(*WHITE)
            self.set_text_color(*DARK_GRAY)

            # Calculate max height for this row
            max_lines = 1
            for i, cell_text in enumerate(row):
                lines_needed = max(1, len(str(cell_text)) * 2 / col_widths[i]) if col_widths[i] > 0 else 1
                # Simple estimate
                text_w = self.get_string_width(str(cell_text))
                lines_needed = max(1, int(text_w / (col_widths[i] - 2)) + 1)
                max_lines = max(max_lines, lines_needed)
            
            cell_h = max(row_h, max_lines * 4)
            if cell_h > 20:
                cell_h = 20  # cap height

            # Check page break
            if self.get_y() + cell_h > 275:
                self.add_page()
                # Re-draw header
                self.set_fill_color(*header_colour)
                self.set_text_color(*WHITE)
                self.set_font('Helvetica', 'B', 8)
                for i, h in enumerate(headers):
                    self.cell(col_widths[i], row_h, f' {h}', border=1, fill=True)
                self.ln(row_h)
                self.set_font('Helvetica', '', 7.5)
                if stripe:
                    self.set_fill_color(*LIGHT_BG)
                else:
                    self.set_fill_color(*WHITE)
                self.set_text_color(*DARK_GRAY)

            x_start = self.get_x()
            y_start = self.get_y()
            for i, cell_text in enumerate(row):
                txt = str(cell_text)[:200]  # Truncate very long text
                self.set_xy(x_start + sum(col_widths[:i]), y_start)
                # Draw cell background
                self.rect(x_start + sum(col_widths[:i]), y_start, col_widths[i], cell_h, 'DF' if stripe else 'D')
                self.set_xy(x_start + sum(col_widths[:i]) + 1, y_start + 1)
                self.multi_cell(col_widths[i] - 2, 4, txt)
            self.set_xy(x_start, y_start + cell_h)
        self.ln(3)


# ══════════════════════════════════════════════════════════════════════
# MAIN: Build the PDF
# ══════════════════════════════════════════════════════════════════════

def build_report():
    pdf = ReportPDF()

    # ── PAGE 1: COVER ──────────────────────────────────────────────────
    pdf.add_page()
    # Big gradient-style background
    for i in range(280):
        r = int(15 + (i / 280) * 20)
        g = int(23 + (i / 280) * 30)
        b = int(42 + (i / 280) * 60)
        pdf.set_draw_color(r, g, b)
        pdf.line(0, i, 210, i)

    # Accent stripe
    pdf.set_fill_color(*BLUE)
    pdf.rect(0, 90, 210, 3, 'F')
    pdf.set_fill_color(*TEAL)
    pdf.rect(0, 93, 210, 1.5, 'F')

    # Title
    pdf.set_xy(20, 105)
    pdf.set_font('Helvetica', 'B', 32)
    pdf.set_text_color(*WHITE)
    pdf.cell(170, 15, 'ECLMS', align='C')

    pdf.set_xy(20, 123)
    pdf.set_font('Helvetica', '', 14)
    pdf.set_text_color(148, 163, 184)
    pdf.cell(170, 8, 'Enterprise Contract Lifecycle Management System', align='C')

    # Divider
    pdf.set_draw_color(*TEAL)
    pdf.set_line_width(0.8)
    pdf.line(60, 138, 150, 138)

    pdf.set_xy(20, 145)
    pdf.set_font('Helvetica', 'B', 18)
    pdf.set_text_color(*WHITE)
    pdf.cell(170, 10, 'Complete Technical Report', align='C')

    pdf.set_xy(20, 158)
    pdf.set_font('Helvetica', '', 12)
    pdf.set_text_color(148, 163, 184)
    pdf.cell(170, 8, 'Architecture, Workflow & Code Walkthrough', align='C')

    # Footer info
    pdf.set_xy(20, 230)
    pdf.set_font('Helvetica', '', 10)
    pdf.set_text_color(100, 116, 139)
    pdf.cell(170, 6, 'Spring Boot 3.3.4  |  Angular 19  |  Java 17  |  MySQL  |  Redis  |  Elasticsearch  |  MinIO', align='C')

    pdf.set_xy(20, 242)
    pdf.set_font('Helvetica', '', 9)
    pdf.cell(170, 6, 'Generated: July 2026', align='C')

    # ── PAGE 2: TABLE OF CONTENTS ──────────────────────────────────────
    pdf.add_page()
    pdf.section_h1('Table of Contents')
    pdf.ln(2)

    toc_items = [
        ('1', 'Project Overview & Architecture'),
        ('2', 'Technology Stack & Dependencies'),
        ('3', 'Complete Workflow — End-to-End Request Flow'),
        ('4', 'Backend: Common / Cross-Cutting Layer'),
        ('4.1', '    Application Entry Point'),
        ('4.2', '    Base Entity & Auditing'),
        ('4.3', '    Configuration Classes (Security, OpenAPI, JPA, DataInit)'),
        ('4.4', '    Security Layer (JWT, Filters, Providers)'),
        ('4.5', '    Exception Handling'),
        ('4.6', '    API Response Wrapper'),
        ('4.7', '    Elasticsearch Search Service'),
        ('4.8', '    MinIO / File Storage Service'),
        ('5', 'Backend: Feature Modules (Detailed)'),
        ('5.1', '    Auth Module'),
        ('5.2', '    User Module'),
        ('5.3', '    Role Module'),
        ('5.4', '    Permission Module'),
        ('5.5', '    Department Module'),
        ('5.6', '    Vendor Module'),
        ('5.7', '    Contract Module'),
        ('5.8', '    Contract Template Module'),
        ('5.9', '    Document Module'),
        ('5.10', '    Workflow Module'),
        ('5.11', '    Dashboard Module'),
        ('5.12', '    Report Module'),
        ('6', 'Frontend (Angular): Architecture & Components'),
        ('7', 'Database Schema & Migrations'),
        ('8', 'Annotations & Decorators Reference Guide'),
    ]

    for num, title in toc_items:
        is_sub = title.startswith('    ')
        if is_sub:
            pdf.set_x(pdf.MARGIN + 10)
            pdf.set_font('Helvetica', '', 9)
        else:
            pdf.set_x(pdf.MARGIN)
            pdf.set_font('Helvetica', 'B', 10)
        pdf.set_text_color(*DARK_GRAY)
        pdf.cell(12, 6, num)
        pdf.cell(0, 6, title.strip())
        pdf.ln(6)

    # ══════════════════════════════════════════════════════════════════
    # SECTION 1: PROJECT OVERVIEW
    # ══════════════════════════════════════════════════════════════════
    pdf.add_page()
    pdf.section_h1('Project Overview & Architecture', '1')

    pdf.body_text(
        'ECLMS (Enterprise Contract Lifecycle Management System) is a full-stack enterprise application '
        'that manages the entire lifecycle of business contracts — from creation through approval workflows '
        'to renewal and termination. The project is organised as a monorepo containing a Spring Boot backend '
        'REST API and an Angular 19 single-page application frontend.'
    )

    pdf.ln(2)
    pdf.section_h3('Technology Stack at a Glance')
    pdf.styled_table(
        ['Layer', 'Technology', 'Directory'],
        [
            ['Backend API', 'Spring Boot 3.3.4 (Java 17)', 'src/main/java/com/company/eclms/'],
            ['Frontend SPA', 'Angular 19 (Standalone Components)', 'ECLMS_FE/src/app/'],
            ['Database', 'MySQL 8+ with Flyway migrations', 'src/main/resources/db/migration/'],
            ['Cache & Sessions', 'Redis', 'External service'],
            ['Full-Text Search', 'Elasticsearch', 'External service'],
            ['Object Storage', 'MinIO (S3-compatible)', 'External service'],
            ['Message Broker', 'RabbitMQ', 'External service (dependency declared)'],
        ],
        col_widths=[30, 60, 84],
    )

    pdf.section_h3('Architectural Pattern — Modular Layered Architecture')
    pdf.body_text(
        'The backend follows a modular layered architecture where each business domain (contract, user, '
        'vendor, workflow, etc.) is an independent module containing its own controller, service, '
        'repository, entity, DTO, and mapper layers. Cross-cutting concerns (security, auditing, exceptions) '
        'reside in the common package.'
    )

    pdf.code_block(
        '  Angular Frontend (SPA)\n'
        '    Guards -> Interceptors -> Services -> Components -> Views\n'
        '  +---------------------------------------------------------+\n'
        '  |              HTTP / REST API (JSON)                      |\n'
        '  +---------------------------------------------------------+\n'
        '  | RateLimitFilter -> JwtAuthFilter -> SecurityContext      |\n'
        '  +---------------------------------------------------------+\n'
        '  |          REST Controllers (@RestController)              |\n'
        '  +---------------------------------------------------------+\n'
        '  |          Service Layer (@Service / Interface+Impl)       |\n'
        '  +---------------------------------------------------------+\n'
        '  |          MapStruct Mappers (@Mapper)                     |\n'
        '  +---------------------------------------------------------+\n'
        '  |          Repository Layer (Spring Data JPA)              |\n'
        '  +---------------------------------------------------------+\n'
        '  | MySQL | Redis | Elasticsearch | MinIO | RabbitMQ         |\n'
        '  +---------------------------------------------------------+'
    )

    pdf.section_h3('Module Internal Structure')
    pdf.body_text(
        'Each business module (e.g., contract, user, vendor) follows the same internal package structure:'
    )
    pdf.code_block(
        '  modules/<module_name>/\n'
        '    controller/    -- REST endpoints\n'
        '    dto/           -- Data Transfer Objects (request/response)\n'
        '    entity/        -- JPA entities (database table mappings)\n'
        '    mapper/        -- MapStruct Entity <-> DTO converters\n'
        '    repository/    -- Spring Data JPA repositories\n'
        '    service/       -- Business logic interface\n'
        '    service/impl/  -- Business logic implementation'
    )

    # ══════════════════════════════════════════════════════════════════
    # SECTION 2: TECHNOLOGY STACK & DEPENDENCIES
    # ══════════════════════════════════════════════════════════════════
    pdf.add_page()
    pdf.section_h1('Technology Stack & Dependencies', '2')

    pdf.section_h3('Build Configuration')
    pdf.styled_table(
        ['Attribute', 'Value'],
        [
            ['Build Tool', 'Maven (pom.xml)'],
            ['Java Version', '17 (LTS)'],
            ['Spring Boot', '3.3.4'],
            ['Group / Artifact', 'com.company / eclms'],
        ],
        col_widths=[40, 134],
    )

    pdf.section_h3('Maven Dependencies — Complete Reference')
    pdf.styled_table(
        ['Dependency', 'Purpose', 'Why Used in This Project'],
        [
            ['spring-boot-starter-web', 'Embeds Tomcat, provides @RestController, Jackson JSON', 'Core of the REST API layer'],
            ['spring-boot-starter-security', 'Authentication & authorization framework', 'Protects all endpoints; provides filter chain'],
            ['spring-boot-starter-validation', 'Bean Validation (Jakarta/Hibernate Validator)', 'Enables @NotBlank, @Valid, @Size on DTOs'],
            ['spring-boot-starter-actuator', 'Health checks & operational monitoring', '/actuator endpoints for DevOps visibility'],
            ['spring-boot-starter-aop', 'Aspect-Oriented Programming support', 'Cross-cutting: logging, security annotations'],
            ['spring-boot-starter-data-jpa', 'JPA + Hibernate ORM integration', 'Maps Java objects to database tables'],
            ['spring-boot-starter-data-redis', 'Redis client (Lettuce-based)', 'Caches tokens, OTPs, rate-limit counters'],
            ['spring-boot-starter-data-elasticsearch', 'Elasticsearch client', 'Full-text search across contracts'],
            ['spring-boot-starter-amqp', 'RabbitMQ integration', 'Async messaging (notifications, audit)'],
            ['mysql-connector-j', 'MySQL JDBC driver', 'Connects Hibernate to MySQL'],
            ['h2', 'In-memory database', 'H2 profile-based local testing'],
            ['flyway-core + flyway-mysql', 'Database migration tool', 'Version-controls DB schema with SQL'],
            ['jjwt-api / impl / jackson', 'JJWT library (JSON Web Token)', 'Creates and validates JWT tokens'],
            ['minio', 'MinIO Java SDK (S3-compatible)', 'Manages contract document storage'],
            ['springdoc-openapi-starter-webmvc-ui', 'Swagger UI + OpenAPI 3', 'Interactive API docs at /swagger-ui.html'],
            ['poi-ooxml', 'Apache POI (Excel generation)', 'Exports reports as .xlsx spreadsheets'],
            ['openpdf', 'PDF generation library (LibrePDF)', 'Exports reports as .pdf documents'],
            ['lombok', 'Compile-time code generation', 'Eliminates boilerplate (getters, setters, builders)'],
            ['mapstruct', 'Compile-time bean mapper', 'Type-safe Entity <-> DTO mapping'],
            ['lombok-mapstruct-binding', 'Lombok + MapStruct interop', 'Ensures correct annotation processor order'],
        ],
        col_widths=[48, 48, 78],
        header_colour=TBL_HEAD2,
    )

    pdf.info_box('Annotation Processor Order',
        'The maven-compiler-plugin configures annotation processors in order: '
        '(1) Lombok runs first to generate getters/setters/constructors, '
        '(2) MapStruct runs second to read Lombok-generated methods and create mapper implementations, '
        '(3) lombok-mapstruct-binding bridges the two so MapStruct sees Lombok\'s output.',
        TEAL
    )

    # ══════════════════════════════════════════════════════════════════
    # SECTION 3: COMPLETE WORKFLOW
    # ══════════════════════════════════════════════════════════════════
    pdf.add_page()
    pdf.section_h1('Complete Workflow — End-to-End Request Flow', '3')

    pdf.section_h2('3.1  Login & Authentication Flow')
    pdf.code_block(
        '  Browser -> POST /api/v1/auth/login { username, password }\n'
        '    |\n'
        '    +-> RateLimitingFilter.doFilterInternal()\n'
        '    |     Check Redis rate-limit counter (key = rate_limit:{user}:{ip})\n'
        '    |     If exceeded -> 429 Too Many Requests\n'
        '    |     If Redis down -> fallback to in-memory ConcurrentHashMap\n'
        '    |\n'
        '    +-> JwtAuthenticationFilter.doFilterInternal()\n'
        '    |     No "Authorization: Bearer ..." header -> skip\n'
        '    |\n'
        '    +-> SecurityFilterChain\n'
        '    |     "/api/v1/auth/**" is permitAll -> reaches controller\n'
        '    |\n'
        '    +-> AuthController.login(LoginRequest, HttpServletRequest)\n'
        '    |     Extracts IP address and User-Agent\n'
        '    |     Delegates to AuthService.login()\n'
        '    |\n'
        '    +-> AuthServiceImpl.login()\n'
        '        +-- Find User by username in DB\n'
        '        +-- Check if account locked (>=5 failures -> 15min lock)\n'
        '        +-- AuthenticationManager.authenticate(credentials)\n'
        '        |     +-> DaoAuthenticationProvider\n'
        '        |     +-> CustomUserDetailsService.loadUserByUsername()\n'
        '        |     +-> BCryptPasswordEncoder.verify()\n'
        '        +-- On success:\n'
        '        |     Generate access token (15 min)\n'
        '        |     Generate refresh token (7 days)\n'
        '        |     Store refresh token in Redis\n'
        '        |     Reset failedLoginAttempts to 0\n'
        '        |     Log to login_histories table\n'
        '        |     Return { accessToken, refreshToken, UserDto }\n'
        '        +-- On failure:\n'
        '              Increment failedLoginAttempts\n'
        '              If >=5 -> lock account\n'
        '              Throw UnauthorizedException'
    )

    pdf.section_h2('3.2  Authenticated API Request Flow')
    pdf.code_block(
        '  Browser -> GET /api/v1/contracts?page=0&size=10\n'
        '  Headers: { Authorization: "Bearer eyJhbGciOi..." }\n'
        '    |\n'
        '    +-> RateLimitingFilter (check rate limit)\n'
        '    +-> JwtAuthenticationFilter\n'
        '    |     parseJwt() -> extract token from "Bearer " header\n'
        '    |     tokenProvider.isTokenExpired(jwt)\n'
        '    |     tokenProvider.extractUsername(jwt)\n'
        '    |     userDetailsService.loadUserByUsername(username)\n'
        '    |     tokenProvider.validateToken(jwt, userDetails)\n'
        '    |     Set authentication in SecurityContextHolder\n'
        '    |\n'
        '    +-> SecurityFilterChain\n'
        '    |     anyRequest().authenticated() -> passes\n'
        '    |\n'
        '    +-> @PreAuthorize("hasAuthority(\'CONTRACT_READ\')")\n'
        '    +-> ContractController -> ContractService\n'
        '    |     Builds JPA Specification (search + filters)\n'
        '    |     contractRepository.findAll(spec, pageable)\n'
        '    |     Maps Page<Contract> -> Page<ContractDto>\n'
        '    |\n'
        '    +-> Response: ApiResponse<Page<ContractDto>>'
    )

    pdf.section_h2('3.3  Contract Lifecycle State Machine')
    pdf.code_block(
        '              +----------+\n'
        '              |  CREATE  |  ContractService.createContract()\n'
        '              |   DRAFT  |  Status="DRAFT", Version="1.0"\n'
        '              +----+-----+\n'
        '                   |\n'
        '                   v\n'
        '         +-------------------+\n'
        '         | START WORKFLOW    |  WorkflowService.startWorkflow()\n'
        '         | -> IN_REVIEW      |  Creates WorkflowInstance\n'
        '         +--------+----------+\n'
        '                  |\n'
        '           +------+------+\n'
        '           v             v\n'
        '     +----------+  +----------+\n'
        '     |  Step 1  |  | REJECTED |  User rejects\n'
        '     |  USER    +->| Contract |  Can be edited\n'
        '     |  APPROVE |  | -> DRAFT |  and resubmitted\n'
        '     +----+-----+  +----------+\n'
        '          | APPROVED\n'
        '          v\n'
        '     +----------+\n'
        '     |  Step 2  |\n'
        '     |  ADMIN   +-> REJECTED (same as above)\n'
        '     |  APPROVE |\n'
        '     +----+-----+\n'
        '          | APPROVED (final step)\n'
        '          v\n'
        '     +----------+\n'
        '     |  ACTIVE  |  Contract is now enforceable\n'
        '     +----+-----+\n'
        '          |\n'
        '     +----+-----+\n'
        '     v          v\n'
        '  +------+  +----------+\n'
        '  |RENEW |  |TERMINATE |\n'
        '  |v +1.0|  |Status =  |\n'
        '  |DRAFT |  |TERMINATED|\n'
        '  +------+  +----------+'
    )

    # ══════════════════════════════════════════════════════════════════
    # SECTION 4: COMMON LAYER
    # ══════════════════════════════════════════════════════════════════
    pdf.add_page()
    pdf.section_h1('Backend: Common / Cross-Cutting Layer', '4')

    # 4.1 Entry Point
    pdf.section_h2('Application Entry Point', '4.1')
    pdf.body_text('File: EclmsApplication.java')
    pdf.code_block(
        '  @SpringBootApplication\n'
        '  public class EclmsApplication {\n'
        '      public static void main(String[] args) {\n'
        '          SpringApplication.run(EclmsApplication.class, args);\n'
        '      }\n'
        '  }'
    )
    pdf.styled_table(
        ['Element', 'Explanation'],
        [
            ['@SpringBootApplication', 'Meta-annotation: @Configuration + @EnableAutoConfiguration + @ComponentScan. Configures beans based on classpath (e.g., sees starter-web -> configures Tomcat).'],
            ['SpringApplication.run()', 'Bootstrap method: creates ApplicationContext, starts Tomcat, runs CommandLineRunner beans, starts listening for HTTP.'],
            ['main(String[] args)', 'Standard Java entry point. args passed to Spring Boot for command-line overrides.'],
        ],
        col_widths=[42, 132],
    )

    # 4.2 Base Entity
    pdf.section_h2('Base Entity & Auditing', '4.2')
    pdf.body_text(
        'BaseEntity is an abstract class providing common fields inherited by EVERY entity. '
        'It implements soft-delete, optimistic locking, and JPA auditing.'
    )
    pdf.styled_table(
        ['Field', 'Annotation(s)', 'Purpose'],
        [
            ['id (UUID)', '@Id, @GeneratedValue(AUTO)', 'Primary key. AUTO lets Hibernate use UUID generator. updatable=false prevents modification.'],
            ['createdAt', '@CreatedDate', 'Auto-populated with creation timestamp. updatable=false ensures immutability.'],
            ['updatedAt', '@LastModifiedDate', 'Auto-updated with current timestamp on each modification.'],
            ['createdBy', '@CreatedBy', 'Auto-populated with authenticated username from SpringSecurityAuditorAware.'],
            ['updatedBy', '@LastModifiedBy', 'Auto-updated with current user on each modification.'],
            ['deleted', '(none)', 'Soft-delete flag. When true, record is hidden but retained for audit.'],
            ['deletedAt/By', '(none)', 'Track when and by whom the soft-delete occurred.'],
            ['version', '@Version', 'Optimistic locking. JPA auto-increments; concurrent updates get OptimisticLockException.'],
        ],
        col_widths=[28, 38, 108],
    )
    pdf.styled_table(
        ['Class Annotation', 'Purpose'],
        [
            ['@Getter / @Setter', 'Lombok: generates all getter/setter methods at compile time.'],
            ['@MappedSuperclass', 'JPA: this class has no table; its fields are inherited by entity subclasses.'],
            ['@EntityListeners(AuditingEntityListener)', 'Spring Data JPA: registers auditing listener for @CreatedDate etc.'],
        ],
        col_widths=[52, 122],
        header_colour=TEAL,
    )

    # 4.3 Configuration
    pdf.add_page()
    pdf.section_h2('Configuration Classes', '4.3')

    pdf.section_h3('SecurityConfig.java')
    pdf.body_text('This is the heart of the security configuration. Key beans and their purposes:')
    pdf.styled_table(
        ['Bean / Method', 'What It Does'],
        [
            ['passwordEncoder()', 'Returns BCryptPasswordEncoder (10 rounds). Hashes passwords with random salt.'],
            ['authenticationProvider()', 'DaoAuthenticationProvider using CustomUserDetailsService + BCrypt.'],
            ['authenticationManager()', 'Exposes AuthenticationManager for programmatic auth in AuthServiceImpl.'],
            ['filterChain(HttpSecurity)', 'Main security filter chain (see details below).'],
            ['corsConfigurationSource()', 'Allows all origins (dev), specific methods and headers.'],
        ],
        col_widths=[42, 132],
    )

    pdf.section_h3('SecurityConfig — Filter Chain Configuration')
    pdf.styled_table(
        ['Configuration', 'Purpose'],
        [
            ['.csrf(disable)', 'Disables CSRF (stateless JWT API, no cookies = no CSRF risk).'],
            ['.cors(corsSource)', 'Enables CORS for Angular frontend on different port.'],
            ['.exceptionHandling(...)', 'Sets custom JSON responses for 401 and 403 errors.'],
            ['.sessionManagement(STATELESS)', 'Never create HTTP sessions; each request must carry JWT.'],
            ['.authorizeHttpRequests()', '/api/v1/auth/**, /swagger-ui/** are public; rest requires auth.'],
            ['addFilterBefore(jwt, UsernamePassword)', 'Inserts JWT filter before default form-login filter.'],
            ['addFilterBefore(rateLimit, Jwt)', 'Rate limiting runs before JWT auth.'],
        ],
        col_widths=[52, 122],
    )

    pdf.section_h3('SecurityConfig — Key Annotations')
    pdf.styled_table(
        ['Annotation', 'Purpose'],
        [
            ['@EnableWebSecurity', 'Enables Spring Security web MVC integration.'],
            ['@EnableMethodSecurity', 'Enables @PreAuthorize / @PostAuthorize on controller methods.'],
            ['@RequiredArgsConstructor', 'Lombok: generates constructor with all final fields for DI.'],
        ],
        col_widths=[45, 129],
        header_colour=GREEN_ACC,
    )

    pdf.section_h3('OpenApiConfig.java')
    pdf.body_text(
        'Configures Swagger/OpenAPI documentation with Bearer JWT authentication scheme. '
        'The @Bean method creates an OpenAPI object with API title, version, description, and a '
        '"bearerAuth" security scheme so developers can test authenticated endpoints from Swagger UI.'
    )

    pdf.section_h3('DataInitializer.java')
    pdf.body_text(
        'Implements CommandLineRunner to seed initial data on application startup:'
    )
    pdf.bullet('Seeds 43 permissions (USER_READ, CONTRACT_CREATE, WORKFLOW_APPROVE, etc.)')
    pdf.bullet('Creates ADMIN and USER roles with all permissions attached')
    pdf.bullet('Creates default admin user (admin/admin123) with BCrypt-encoded password')
    pdf.bullet('Creates 4 default two-step sequential approval workflows')
    pdf.bullet('Uses findByName().orElseGet() pattern for idempotent seeding')
    pdf.bullet('@Transactional ensures all-or-nothing database consistency')

    # 4.4 Security
    pdf.add_page()
    pdf.section_h2('Security Layer — JWT, Filters & Providers', '4.4')

    pdf.section_h3('JwtTokenProvider.java')
    pdf.styled_table(
        ['Method', 'Purpose'],
        [
            ['@PostConstruct init()', 'Decodes JWT secret (Base64 or raw bytes), creates HMAC-SHA key. Runs once after DI.'],
            ['generateAccessToken(UserDetails)', 'Creates JWT: subject=username, roles claim, expiry=15min, signed with HMAC-SHA.'],
            ['generateRefreshToken(UserDetails)', 'Creates JWT: subject only, no roles, expiry=7 days.'],
            ['createToken(claims, subject, expiryMs)', 'Private: builds JWT with Jwts.builder(), signs and compacts.'],
            ['extractUsername(token)', 'Parses JWT, extracts "sub" (subject) claim.'],
            ['extractClaim(token, Function)', 'Generic claim extractor using functional programming pattern.'],
            ['isTokenExpired(token)', 'Returns true if expiration date is in the past. Treats invalid tokens as expired.'],
            ['validateToken(token, userDetails)', 'Validates: username matches AND not expired.'],
        ],
        col_widths=[50, 124],
    )

    pdf.section_h3('JwtAuthenticationFilter.java')
    pdf.body_text(
        'Extends OncePerRequestFilter (guarantees exactly once per request). Core logic:'
    )
    pdf.bullet('parseJwt(): extracts raw JWT from "Authorization: Bearer <token>" header')
    pdf.bullet('Validates token is not expired')
    pdf.bullet('Loads UserDetails from database via CustomUserDetailsService')
    pdf.bullet('Creates UsernamePasswordAuthenticationToken with authorities')
    pdf.bullet('Sets authentication in SecurityContextHolder (thread-local)')

    pdf.section_h3('RateLimitingFilter.java')
    pdf.body_text('Implements a fixed-window rate limiter with dual-mode operation:')
    pdf.styled_table(
        ['Mode', 'How It Works'],
        [
            ['Redis (Primary)', 'Key = "rate_limit:{user}:{ip}". If null, set to 1 with 1-min TTL. If < limit, increment. If >= limit, reject (429).'],
            ['In-Memory (Fallback)', 'ConcurrentHashMap<String, RequestBucket>. Each bucket has AtomicInteger counter + AtomicLong window timestamp. Resets after 60s.'],
        ],
        col_widths=[35, 139],
    )

    pdf.section_h3('CustomUserDetailsService.java')
    pdf.body_text(
        'Implements UserDetailsService (Spring Security SPI). The loadUserByUsername() method is called by '
        'DaoAuthenticationProvider during login. It loads the User from DB, iterates over roles, and builds '
        'authorities list with both ROLE_ADMIN (role-based) and CONTRACT_READ (permission-based) entries.'
    )

    # 4.5 Exception Handling
    pdf.section_h2('Exception Handling', '4.5')
    pdf.body_text(
        'GlobalExceptionHandler uses @RestControllerAdvice to intercept all exceptions globally.'
    )
    pdf.styled_table(
        ['Exception', 'HTTP Status', 'Scenario'],
        [
            ['BusinessException', 'Dynamic (from ex)', 'Generic business rule violation'],
            ['MethodArgumentNotValidException', '400 Bad Request', '@Valid validation fails on DTO'],
            ['AccessDeniedException', '403 Forbidden', '@PreAuthorize check fails'],
            ['BadCredentialsException', '401 Unauthorized', 'Wrong username/password'],
            ['Exception (catch-all)', '500 Internal Server Error', 'Any unhandled exception'],
        ],
        col_widths=[48, 35, 91],
    )

    pdf.section_h3('Custom Exception Hierarchy')
    pdf.styled_table(
        ['Exception', 'Extends', 'Status', 'Usage'],
        [
            ['NotFoundException', 'BusinessException', '404', 'Entity not found'],
            ['ConflictException', 'BusinessException', '409', 'State conflict (e.g. updating active contract)'],
            ['UnauthorizedException', 'BusinessException', '401', 'Invalid credentials, expired token'],
            ['ForbiddenException', 'BusinessException', '403', 'Insufficient permissions'],
            ['ValidationException', 'BusinessException', '400', 'Custom validation failures'],
            ['StorageException', 'BusinessException', '500', 'MinIO/file storage errors'],
            ['DocumentException', 'BusinessException', '500', 'Document processing errors'],
            ['WorkflowException', 'BusinessException', '400', 'Workflow state violations'],
            ['VendorException', 'BusinessException', '400', 'Vendor-related errors'],
            ['AIException', 'BusinessException', '500', 'AI/ML integration errors'],
        ],
        col_widths=[36, 32, 14, 92],
        header_colour=RED_ACC,
    )

    # 4.6 API Response
    pdf.add_page()
    pdf.section_h2('API Response Wrapper', '4.6')
    pdf.code_block(
        '  @Data @Builder @NoArgsConstructor @AllArgsConstructor\n'
        '  public class ApiResponse<T> {\n'
        '      private boolean success;\n'
        '      private String message;\n'
        '      private T data;\n'
        '      private List<String> errors;\n'
        '      private LocalDateTime timestamp;\n'
        '  }'
    )
    pdf.styled_table(
        ['Factory Method', 'Purpose'],
        [
            ['success(T data, String msg)', 'Creates success response: success=true, message, data payload, timestamp.'],
            ['success(T data)', 'Overload with default message "Operation completed successfully".'],
            ['error(List<String>, String msg)', 'Creates error response: success=false, error list, message, timestamp.'],
            ['error(String error, String msg)', 'Convenience overload wrapping single error into a list.'],
        ],
        col_widths=[48, 126],
    )
    pdf.info_box('Why a generic wrapper?',
        'Every API endpoint returns the same structure, so the frontend can consistently check '
        'response.success and access response.data or response.errors regardless of the endpoint.',
        GREEN_ACC
    )

    # 4.7 Elasticsearch
    pdf.section_h2('Elasticsearch Search Service', '4.7')
    pdf.styled_table(
        ['Method', 'Purpose'],
        [
            ['indexContract(id, name, content, vendor, status, ocr)', 'Indexes a contract in Elasticsearch via PUT /contracts/_doc/{id}. Graceful degradation if ES is down.'],
            ['deleteIndex(contractId)', 'Removes a contract from the ES index when soft-deleted.'],
            ['searchContractIds(searchTerm)', 'multi_match query across name (boost x3), content, vendorName (boost x2), ocrText. Returns matching UUIDs.'],
        ],
        col_widths=[52, 122],
    )

    # 4.8 MinIO
    pdf.section_h2('MinIO / File Storage Service', '4.8')
    pdf.styled_table(
        ['Method', 'Purpose'],
        [
            ['@PostConstruct init()', 'Connects to MinIO, creates default bucket. If fails -> activates local filesystem fallback.'],
            ['uploadFile(bucket, name, stream, size, type)', 'Normal: minioClient.putObject(). Fallback: writes to logs/minio-fallback/.'],
            ['downloadFile(bucket, name)', 'Normal: minioClient.getObject(). Fallback: Files.newInputStream() from local.'],
            ['getPreviewUrl(bucket, name)', 'Normal: generates pre-signed URL (2h expiry). Fallback: returns file:/// URL.'],
            ['deleteFile(bucket, name)', 'Removes file from MinIO or local filesystem.'],
        ],
        col_widths=[52, 122],
    )

    # ══════════════════════════════════════════════════════════════════
    # SECTION 5: FEATURE MODULES
    # ══════════════════════════════════════════════════════════════════
    pdf.add_page()
    pdf.section_h1('Backend: Feature Modules (Detailed)', '5')

    # 5.1 Auth
    pdf.section_h2('Auth Module', '5.1')
    pdf.body_text('Package: com.company.eclms.modules.auth')
    pdf.section_h3('AuthController — Endpoints')
    pdf.styled_table(
        ['Endpoint', 'Method', 'Purpose'],
        [
            ['POST /api/v1/auth/register', 'register()', 'Public registration. Validates UserRegistrationDto with @Valid.'],
            ['POST /api/v1/auth/login', 'login()', 'Authenticates user, returns JWT tokens. Captures IP and User-Agent.'],
            ['POST /api/v1/auth/refresh', 'refresh()', 'Takes refresh token, issues new token pair (token rotation).'],
            ['POST /api/v1/auth/logout', 'logout()', 'Blacklists access token in Redis, revokes refresh token.'],
            ['POST /api/v1/auth/otp/send', 'sendOtp()', 'Generates 6-digit OTP, stores in Redis (5 min TTL).'],
            ['POST /api/v1/auth/password/reset', 'resetPassword()', 'Verifies OTP, resets password with BCrypt hash.'],
        ],
        col_widths=[52, 24, 98],
    )

    pdf.section_h3('AuthServiceImpl — Key Features')
    pdf.bullet('Account Lockout: After 5 failed login attempts, account is locked for 15 minutes')
    pdf.bullet('Token Rotation: On refresh, old refresh token is replaced to prevent replay attacks')
    pdf.bullet('Token Blacklisting: On logout, access token is blacklisted in Redis for 15 minutes')
    pdf.bullet('OTP System: 6-digit codes stored in Redis with 5-min TTL, in-memory fallback')
    pdf.bullet('Login History: Each attempt logged to login_histories table via JdbcTemplate')
    pdf.bullet('Redis Fallback: All Redis ops wrapped in try-catch with in-memory ConcurrentHashMap')

    # 5.2 User
    pdf.add_page()
    pdf.section_h2('User Module', '5.2')
    pdf.section_h3('User Entity — Fields')
    pdf.styled_table(
        ['Field', 'Type / Annotation', 'Purpose'],
        [
            ['username', 'String, UNIQUE', 'Login credential'],
            ['email', 'String, UNIQUE', 'For OTP-based password reset'],
            ['password', 'String', 'BCrypt-encoded hash'],
            ['fullName', 'String', 'Display name'],
            ['status', 'String (ACTIVE)', 'ACTIVE, INACTIVE, SUSPENDED'],
            ['locked / lockedUntil', 'boolean / LocalDateTime', 'Account lockout state'],
            ['failedLoginAttempts', 'int (default 0)', 'Brute-force protection counter'],
            ['department', '@ManyToOne(LAZY)', 'User belongs to one department'],
            ['roles', '@ManyToMany(EAGER)', 'User can have multiple roles (via user_roles join table)'],
        ],
        col_widths=[38, 38, 98],
    )

    pdf.section_h3('User Entity — JPA Annotations Explained')
    pdf.styled_table(
        ['Annotation', 'Purpose'],
        [
            ['@Entity', 'Marks this class as a JPA entity (maps to a database table).'],
            ['@Table(name = "users")', 'Explicitly names the table.'],
            ['@SQLDelete(sql = "UPDATE...")', 'Hibernate: intercepts DELETE and executes soft-delete UPDATE instead.'],
            ['@SQLRestriction("deleted = false")', 'Hibernate 6.3+: auto-adds WHERE deleted=false to all queries.'],
            ['@ManyToMany + @JoinTable', 'Many-to-many via user_roles join table. EAGER loads roles with user.'],
            ['@ManyToOne(fetch = LAZY)', 'Department loaded only when accessed (performance optimization).'],
        ],
        col_widths=[52, 122],
        header_colour=GREEN_ACC,
    )

    # 5.7 Contract
    pdf.add_page()
    pdf.section_h2('Contract Module (Core Business)', '5.7')
    pdf.section_h3('Contract Entity — Fields')
    pdf.styled_table(
        ['Field', 'Purpose'],
        [
            ['name', 'Contract title'],
            ['vendor (@ManyToOne)', 'The vendor party to the contract'],
            ['department (@ManyToOne)', 'The owning department'],
            ['template (@ManyToOne)', 'Optional reference to template used for content generation'],
            ['content (TEXT)', 'Actual contract text (may be interpolated from template)'],
            ['status', 'Lifecycle: DRAFT -> IN_REVIEW -> ACTIVE/REJECTED -> EXPIRED/TERMINATED'],
            ['startDate / endDate / renewalDate', 'Contract temporal bounds'],
            ['riskScore (DECIMAL)', 'Risk assessment value'],
            ['metadata (TEXT)', 'Free-form JSON/text metadata'],
            ['versionString', 'Human-readable version (e.g., "1.0", "2.0" after renewal)'],
        ],
        col_widths=[46, 128],
    )

    pdf.section_h3('ContractServiceImpl — Key Methods')
    pdf.styled_table(
        ['Method', 'Business Logic'],
        [
            ['createContract()', 'Resolves vendor + dept by ID. If template: loads + interpolates {{variables}}. Saves DRAFT v1.0.'],
            ['getContracts()', 'Builds JPA Specification dynamically: status, dept, vendor, text search (LIKE). Returns paginated DTOs.'],
            ['updateContract()', 'Only DRAFT/REJECTED contracts. Re-interpolates template if variables changed.'],
            ['deleteContract()', 'Soft-delete via contractRepository.softDeleteById().'],
            ['renewContract()', 'Only ACTIVE/EXPIRED. Version +1.0, extends dates, renewalDate = endDate -1mo, status -> DRAFT.'],
            ['terminateContract()', 'Sets status to TERMINATED.'],
            ['interpolate(content, vars)', 'Replaces {{key}} placeholders with provided values from template variables map.'],
        ],
        col_widths=[38, 136],
    )

    # 5.8 Template
    pdf.section_h2('Contract Template Module', '5.8')
    pdf.body_text(
        'Templates contain reusable contract text with {{variable}} placeholders. '
        'Status lifecycle: DRAFT -> PUBLISHED -> ARCHIVED. '
        'Variables are auto-extracted from content via regex \\{\\{([^}]+)\\}\\}.'
    )

    # 5.9 Document
    pdf.section_h2('Document Module', '5.9')
    pdf.body_text(
        'Manages file uploads/downloads linked to contracts. Key fields: fileName, filePath (MinIO/local), '
        'fileType (MIME), checksum (SHA-256), docSize, ocrText, versionNumber. '
        'Upload flow: compute SHA-256 -> upload to MinIO via StorageService -> save metadata to DB.'
    )

    # 5.10 Workflow
    pdf.add_page()
    pdf.section_h2('Workflow Module — Multi-Step Approval Engine', '5.10')
    pdf.body_text('The most complex module — implements a configurable multi-step approval engine.')

    pdf.section_h3('Workflow Entities')
    pdf.styled_table(
        ['Entity', 'Purpose'],
        [
            ['Workflow', 'Reusable workflow template (e.g. "Standard Vendor Approval"). Contains WorkflowStep list.'],
            ['WorkflowStep', 'Single step: stepNumber, stepType (SEQUENTIAL), assigneeRole, assigneeUser, requiredApprovals.'],
            ['WorkflowInstance', 'Running instance tied to a contract. Tracks currentStepNumber, status (IN_PROGRESS/APPROVED/REJECTED).'],
            ['WorkflowApproval', 'Individual action by a user: stepNumber, user, status (APPROVED/REJECTED), comments, actionDate.'],
        ],
        col_widths=[36, 138],
    )

    pdf.section_h3('approveOrRejectStep() — Core Approval Logic')
    pdf.body_text('This is the most complex method in the entire application:')
    pdf.bullet('1. Validates instance is IN_PROGRESS')
    pdf.bullet('2. Gets current step from workflow template by stepNumber')
    pdf.bullet('3. Authorization check: user has required role OR is specific assignee OR is ADMIN')
    pdf.bullet('4. Records the WorkflowApproval (APPROVED or REJECTED)')
    pdf.bullet('5. If REJECTED: instance REJECTED, contract REJECTED')
    pdf.bullet('6. If APPROVED: counts approvals for current step. If enough:')
    pdf.bullet('   - If more steps remain: advance to next step (currentStepNumber + 1)', indent=12)
    pdf.bullet('   - If last step: instance APPROVED, contract -> ACTIVE', indent=12)

    # 5.11 Dashboard
    pdf.section_h2('Dashboard Module', '5.11')
    pdf.body_text(
        'GET /api/v1/dashboard/stats returns aggregated statistics: total contracts, active contracts, '
        'pending (in-review), total vendors, total users, total templates, total documents, '
        'upcoming renewals (within 30 days). Queries multiple repositories.'
    )

    # 5.12 Report
    pdf.section_h2('Report Module', '5.12')
    pdf.styled_table(
        ['Endpoint', 'Format', 'Library Used'],
        [
            ['GET /reports/export/excel', '.xlsx', 'Apache POI (XSSFWorkbook)'],
            ['GET /reports/export/pdf', '.pdf', 'OpenPDF (PdfWriter / PdfPTable)'],
            ['GET /reports/export/csv', '.csv', 'StringBuilder with CSV escaping'],
        ],
        col_widths=[50, 20, 104],
    )

    # ══════════════════════════════════════════════════════════════════
    # SECTION 6: FRONTEND
    # ══════════════════════════════════════════════════════════════════
    pdf.add_page()
    pdf.section_h1('Frontend (Angular): Architecture & Components', '6')

    pdf.body_text(
        'The frontend is an Angular 19 SPA using standalone components (no NgModules). '
        'It uses functional guards and interceptors (Angular 15+ pattern).'
    )

    pdf.section_h3('Directory Structure')
    pdf.code_block(
        '  ECLMS_FE/src/app/\n'
        '  +-- core/                   # Singleton services, guards, interceptors\n'
        '  |   +-- guards/auth.guard.ts\n'
        '  |   +-- interceptors/jwt.interceptor.ts\n'
        '  |   +-- services/\n'
        '  |       +-- auth.service.ts, contract.service.ts, dashboard.service.ts\n'
        '  |       +-- department.service.ts, document.service.ts, report.service.ts\n'
        '  |       +-- template.service.ts, vendor.service.ts, workflow.service.ts\n'
        '  +-- features/               # Feature-specific components\n'
        '  |   +-- contracts/ dashboard/ documents/ login/\n'
        '  |   +-- metadata/ register/ templates/ workflows/\n'
        '  +-- shared/layout/          # Sidebar + navbar layout wrapper'
    )

    pdf.section_h3('Routing Configuration')
    pdf.styled_table(
        ['Path', 'Component', 'Guard', 'Purpose'],
        [
            ['/login', 'LoginComponent', 'None', 'Public login page'],
            ['/register', 'RegisterComponent', 'None', 'Public registration'],
            ['/ (layout)', 'LayoutComponent', 'authGuard', 'Protected layout wrapper'],
            ['/dashboard', 'DashboardComponent', '(inherited)', 'Statistics dashboard'],
            ['/contracts', 'ContractListComponent', '(inherited)', 'Contract list with search/filter'],
            ['/contracts/new', 'ContractFormComponent', '(inherited)', 'Create new contract'],
            ['/contracts/edit/:id', 'ContractFormComponent', '(inherited)', 'Edit existing contract'],
            ['/workflows', 'WorkflowListComponent', '(inherited)', 'Workflow instances & approvals'],
            ['/documents', 'DocumentListComponent', '(inherited)', 'Document upload/download'],
            ['/templates', 'TemplateListComponent', '(inherited)', 'Template CRUD management'],
            ['/metadata', 'MetadataListComponent', '(inherited)', 'Depts, Vendors, Roles, Perms CRUD'],
        ],
        col_widths=[36, 40, 24, 74],
    )

    pdf.section_h3('Auth Guard (Functional)')
    pdf.code_block(
        '  export const authGuard: CanActivateFn = (route, state) => {\n'
        '    const authService = inject(AuthService);\n'
        '    const router = inject(Router);\n'
        '    if (authService.isLoggedIn()) return true;\n'
        '    router.navigate([\'/login\']);\n'
        '    return false;\n'
        '  };'
    )

    pdf.section_h3('JWT Interceptor (Functional)')
    pdf.code_block(
        '  export const jwtInterceptor: HttpInterceptorFn = (req, next) => {\n'
        '    const token = localStorage.getItem(\'accessToken\');\n'
        '    if (token) {\n'
        '      req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });\n'
        '    }\n'
        '    return next(req);\n'
        '  };'
    )
    pdf.body_text(
        'Purpose: auto-attaches JWT to every outgoing HTTP request. Uses req.clone() because '
        'Angular HttpRequest objects are immutable.'
    )

    pdf.section_h3('Service Layer Pattern')
    pdf.body_text(
        'All services follow the same pattern: @Injectable({ providedIn: "root" }) for singleton registration, '
        'inject(HttpClient) for modern DI, environment.apiUrl for base URL, '
        'and Observable<any> return types for lazy asynchronous HTTP calls.'
    )

    # ══════════════════════════════════════════════════════════════════
    # SECTION 7: DATABASE SCHEMA
    # ══════════════════════════════════════════════════════════════════
    pdf.add_page()
    pdf.section_h1('Database Schema & Migrations', '7')
    pdf.body_text('Schema managed by Flyway migrations at src/main/resources/db/migration/mysql/.')

    pdf.section_h3('Entity Relationship Diagram')
    pdf.code_block(
        '  permissions <-- role_permissions --> roles\n'
        '                                       |\n'
        '                   user_roles ----------+\n'
        '                      |\n'
        '  departments <---- users -----> teams / team_members\n'
        '       |              |\n'
        '       |        +-----+------+\n'
        '       |        |            |\n'
        '       +----> contracts     vendors\n'
        '                |    |\n'
        '             +--+    +-----> contract_templates\n'
        '             |\n'
        '          documents       workflow_instances\n'
        '                               |\n'
        '                          workflow_approvals\n'
        '  \n'
        '  workflows --> workflow_steps\n'
        '  audit_logs, login_histories (standalone)'
    )

    pdf.section_h3('Key Database Indexes')
    pdf.styled_table(
        ['Index Name', 'Table', 'Column(s)', 'Purpose'],
        [
            ['idx_contracts_vendor', 'contracts', 'vendor_id', 'Fast joins when filtering by vendor'],
            ['idx_contracts_department', 'contracts', 'department_id', 'Fast joins when filtering by department'],
            ['idx_contracts_status', 'contracts', 'status', 'Fast filtering by contract status'],
            ['idx_users_username', 'users', 'username', 'Fast login lookups'],
            ['idx_users_email', 'users', 'email', 'Fast OTP/password-reset lookups'],
            ['idx_audit_entity', 'audit_logs', 'entity_name, entity_id', 'Fast audit trail lookups'],
        ],
        col_widths=[40, 25, 40, 69],
    )

    # ══════════════════════════════════════════════════════════════════
    # SECTION 8: ANNOTATIONS REFERENCE
    # ══════════════════════════════════════════════════════════════════
    pdf.add_page()
    pdf.section_h1('Annotations & Decorators Reference Guide', '8')

    pdf.section_h2('Java / Spring Boot Annotations', '8.1')
    pdf.styled_table(
        ['Annotation', 'Type', 'Purpose'],
        [
            ['@SpringBootApplication', 'Spring Boot', '@Configuration + @EnableAutoConfiguration + @ComponentScan'],
            ['@Configuration', 'Spring', 'Marks class as source of @Bean definitions'],
            ['@Bean', 'Spring', 'Method return value registered as Spring bean'],
            ['@Component', 'Spring', 'Generic Spring-managed component'],
            ['@Service', 'Spring', 'Service-layer specialization of @Component'],
            ['@Repository', 'Spring', 'Data-access specialization; translates persistence exceptions'],
            ['@RestController', 'Spring Web', '@Controller + @ResponseBody; returns JSON'],
            ['@RequestMapping', 'Spring Web', 'Maps HTTP requests to handler methods/classes'],
            ['@GetMapping / @PostMapping etc.', 'Spring Web', 'Shorthand for @RequestMapping(method=...)'],
            ['@RequestBody', 'Spring Web', 'Deserializes HTTP body JSON into Java object'],
            ['@RequestParam', 'Spring Web', 'Binds query parameters to method params'],
            ['@PathVariable', 'Spring Web', 'Extracts path segments into method params'],
            ['@Valid', 'Jakarta', 'Triggers bean validation on parameter'],
            ['@NotBlank / @Size / @Email', 'Jakarta', 'Field validation constraints'],
            ['@Entity', 'JPA', 'Marks class as persistent entity (DB table)'],
            ['@Table', 'JPA', 'Specifies table name'],
            ['@Id / @GeneratedValue', 'JPA', 'Primary key and generation strategy'],
            ['@Column', 'JPA', 'Customizes column mapping'],
            ['@ManyToOne / @OneToMany', 'JPA', 'Relationship mappings'],
            ['@ManyToMany / @JoinTable', 'JPA', 'Many-to-many via join table'],
            ['@MappedSuperclass', 'JPA', 'No table; fields inherited by subclasses'],
            ['@Version', 'JPA', 'Optimistic locking with auto-increment'],
            ['@CreatedDate / @LastModifiedDate', 'Spring Data', 'Auto-populate audit timestamps'],
            ['@CreatedBy / @LastModifiedBy', 'Spring Data', 'Auto-populate audit usernames'],
            ['@SQLDelete / @SQLRestriction', 'Hibernate', 'Soft-delete and auto-filtering'],
            ['@EnableJpaAuditing', 'Spring Data JPA', 'Activates auditing infrastructure'],
            ['@EnableWebSecurity', 'Spring Security', 'Enables web security configuration'],
            ['@EnableMethodSecurity', 'Spring Security', 'Enables @PreAuthorize annotations'],
            ['@PreAuthorize', 'Spring Security', 'SpEL authorization check before method'],
            ['@Transactional', 'Spring TX', 'Wraps method in DB transaction'],
            ['@Value("${...}")', 'Spring', 'Injects property values'],
            ['@PostConstruct', 'Jakarta', 'Method runs once after DI'],
            ['@Slf4j', 'Lombok', 'Generates Logger field'],
            ['@Data / @Getter / @Setter', 'Lombok', 'Generates getters, setters, toString, etc.'],
            ['@Builder', 'Lombok', 'Generates Builder pattern'],
            ['@RequiredArgsConstructor', 'Lombok', 'Constructor with final fields (for DI)'],
            ['@Mapper(componentModel="spring")', 'MapStruct', 'Compile-time bean mapper as Spring bean'],
            ['@RestControllerAdvice', 'Spring', 'Global exception handler for REST controllers'],
            ['@ExceptionHandler', 'Spring', 'Maps exception type to handler method'],
        ],
        col_widths=[52, 26, 96],
        header_colour=TBL_HEAD,
    )

    pdf.add_page()
    pdf.section_h2('TypeScript / Angular Decorators', '8.2')
    pdf.styled_table(
        ['Decorator / Function', 'Purpose'],
        [
            ['@Component', 'Marks class as Angular component (selector, template, styles)'],
            ['@Injectable', 'Marks class as injectable for Angular DI'],
            ['providedIn: "root"', 'Registers service as singleton in root injector (tree-shakeable)'],
            ['standalone: true', 'Component does not belong to any NgModule'],
            ['inject(Service)', 'Angular inject function: retrieves DI dependency (functional DI)'],
            ['CanActivateFn', 'Type for functional route guards (Angular 15+)'],
            ['HttpInterceptorFn', 'Type for functional HTTP interceptors (Angular 15+)'],
            ['Observable<T>', 'RxJS: lazy asynchronous data stream'],
            ['BehaviorSubject<T>', 'RxJS: observable storing latest value, emits to new subscribers'],
        ],
        col_widths=[44, 130],
        header_colour=TBL_HEAD2,
    )

    # ══════════════════════════════════════════════════════════════════
    # FINAL: SUMMARY PAGE
    # ══════════════════════════════════════════════════════════════════
    pdf.add_page()
    pdf.section_h1('Summary — Production-Grade Feature Checklist')

    features = [
        ('JWT Authentication', 'Stateless access/refresh token rotation with HMAC-SHA signing'),
        ('RBAC Authorization', '43 fine-grained permissions across ADMIN and USER roles'),
        ('Multi-Step Workflows', 'Configurable sequential approval engine with role-based authorization'),
        ('Soft-Delete', 'All entities support logical deletion with audit trail (deleted/deletedAt/deletedBy)'),
        ('Optimistic Locking', '@Version field prevents concurrent lost updates'),
        ('Rate Limiting', 'Redis-based fixed-window limiter (60 req/min) with in-memory fallback'),
        ('Full-Text Search', 'Elasticsearch multi_match across contract fields with field boosting'),
        ('Object Storage', 'MinIO (S3-compatible) with automatic local filesystem fallback'),
        ('Report Generation', 'Excel (.xlsx), PDF, and CSV export of contract data'),
        ('Database Migrations', 'Flyway version-controlled SQL migrations for MySQL'),
        ('API Documentation', 'Swagger UI / OpenAPI 3 with JWT Bearer auth support'),
        ('Template Engine', 'Contract generation from templates with {{variable}} interpolation'),
        ('Account Lockout', 'Auto-lock after 5 failed logins; auto-unlock after 15 minutes'),
        ('OTP Password Reset', '6-digit OTP via Redis (5-min TTL) with in-memory fallback'),
        ('Graceful Degradation', 'Redis, Elasticsearch, MinIO failures do NOT crash the application'),
        ('JPA Auditing', 'Automatic createdAt/updatedAt/createdBy/updatedBy on all entities'),
        ('Login History', 'Every login attempt (success/failure) recorded for compliance audit'),
    ]

    pdf.styled_table(
        ['Feature', 'Implementation Detail'],
        features,
        col_widths=[38, 136],
        header_colour=GREEN_ACC,
    )

    # ── Save ───────────────────────────────────────────────────────────
    output_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'ECLMS_Technical_Report.pdf')
    pdf.output(output_path)
    print(f'\n  PDF generated successfully!')
    print(f'  Location: {output_path}')
    print(f'  Pages: {pdf.page_no()}')
    return output_path

if __name__ == '__main__':
    build_report()
