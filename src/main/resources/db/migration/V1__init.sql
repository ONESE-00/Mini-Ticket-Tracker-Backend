-- Tickets ------------------------------------------------------------------
CREATE TABLE tickets (
                         id          UUID         PRIMARY KEY,
                         title       VARCHAR(200) NOT NULL,
                         description TEXT,
                         status      VARCHAR(20)  NOT NULL,
                         priority    VARCHAR(20)  NOT NULL,
                         created_at  TIMESTAMPTZ  NOT NULL,
                         updated_at  TIMESTAMPTZ  NOT NULL,
                         CONSTRAINT chk_tickets_status   CHECK (status   IN ('open', 'in_progress', 'closed')),
                         CONSTRAINT chk_tickets_priority CHECK (priority IN ('low', 'medium', 'high'))
);

-- Indexes to support the list filters/sort the frontend sends.
CREATE INDEX idx_tickets_status     ON tickets (status);
CREATE INDEX idx_tickets_priority   ON tickets (priority);
CREATE INDEX idx_tickets_created_at ON tickets (created_at DESC);

-- Ticket history (audit trail) --------------------------------------------
CREATE TABLE ticket_history (
                                id         UUID        PRIMARY KEY,
                                ticket_id  UUID        NOT NULL REFERENCES tickets (id) ON DELETE CASCADE,
                                action     VARCHAR(20) NOT NULL,
                                field      VARCHAR(20),
                                from_value TEXT,
                                to_value   TEXT,
                                changed_at TIMESTAMPTZ NOT NULL,
                                CONSTRAINT chk_history_action CHECK (action IN ('created', 'updated', 'status_changed')),
      CONSTRAINT chk_history_field  CHECK (field  IN ('title', 'description', 'priority', 'status'))
  );

CREATE INDEX idx_history_ticket_changed
    ON ticket_history (ticket_id, changed_at DESC);