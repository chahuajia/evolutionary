/**
 * 商城促销工作台。
 */

import { PageHeader } from "@/components/page-header";
import { MallWorkspace } from "./mall-workspace";

export default function MallPage() {
  return (
    <>
      <PageHeader
        eyebrow="消费者 · 商城"
        title="商城促销"
        description="领券 → 下单 / 带券结账。与换电权益账本分离（独立 MallOrder）。"
      />
      <MallWorkspace />
    </>
  );
}
