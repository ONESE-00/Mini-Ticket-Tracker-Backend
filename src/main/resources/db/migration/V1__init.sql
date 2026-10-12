CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status varchar(20) NOT NULL
                     CONSTRAINT  check_tickets_status
                     CHECK ( status IN ('open', 'in_progress', 'closed') ),
    priority varchar(20) NOT NULL
                     constraint check_tickets_priority
                     CHECK ( status in ('low', 'medium','high') ),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

--Indexes to support list filters
CREATE INDEX idx_tickets_status ON tickets (status);
CREATE INDEX idx_tickets_priority ON tickets (priority);
CREATE INDEX idx_tickets_created_at ON tickets (created_at DESC);

CREATE TABLE ticket_history (
    id          UUID PRIMARY KEY,
    ticket_id   UUID NOT NULL
                            REFERENCES tickets(id) ON DELETE CASCADE ,
    action      varchar(20) NOT NULL
                CONSTRAINT check_history_action
                CHECK ( action IN ('created', 'updated', 'status_changed') ),
    field varchar(20)
            CONSTRAINT check_history_field
            CHECK ( field in ('title', 'description', 'priority', 'status'))
    from_value TEXT,
    to_value TEXT,
    changed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_history_ticket_changed ON ticket_history ( ticket_id, changed_at DESC);