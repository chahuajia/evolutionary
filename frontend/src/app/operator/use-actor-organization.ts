"use client";

/**
 * 操作方组织门 — GET /operator/orgs/{id} 对齐 canActAsManager（仅 ACTIVE）。
 */

import { useEffect, useMemo, useState } from "react";
import {
  toOrganizationView,
  type OrganizationView,
} from "@/domains/operator/domain/organization-view";
import { fetchOrganization } from "@/domains/operator/infrastructure/operator-gateway";

export type ActorOrganizationGate = {
  readonly actor: OrganizationView | null;
  readonly canAct: boolean;
  readonly blockMessage: string | null;
  readonly statusLabel: string | null;
};

export function useActorOrganization(
  actorOrgId: string,
  fallbackId: string,
): ActorOrganizationGate {
  const [actor, setActor] = useState<OrganizationView | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    const id = actorOrgId.trim() || fallbackId;
    setLoadError(null);
    fetchOrganization(id)
      .then((dto) => {
        if (cancelled) return;
        setActor(
          toOrganizationView({
            id: dto.id,
            name: dto.name,
            parentId: dto.parentId,
            status: dto.status,
            operatorCapability: dto.operatorCapability,
          }),
        );
      })
      .catch((err) => {
        if (cancelled) return;
        setActor(null);
        setLoadError(err instanceof Error ? err.message : String(err));
      });
    return () => {
      cancelled = true;
    };
  }, [actorOrgId, fallbackId]);

  return useMemo(() => {
    if (!actor) {
      return {
        actor: null,
        canAct: false,
        blockMessage: loadError ?? "正在加载操作方组织…",
        statusLabel: null,
      };
    }
    return {
      actor,
      canAct: actor.canActAsManager,
      blockMessage: actor.blockMessage,
      statusLabel: actor.statusLabel,
    };
  }, [actor, loadError]);
}
