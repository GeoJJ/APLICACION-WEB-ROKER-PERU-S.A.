import { ShieldPlus, Hand, SprayCan, Shirt, Package } from "lucide-react";

// Cuando tengas las fotos, agrega: image: mascarillas (importada desde assets)
export const CATEGORIES = [
    { id: "mascarillas", name: "Mascarillas", icon: ShieldPlus },
    { id: "guantes", name: "Guantes", icon: Hand },
    { id: "desinfectantes", name: "Desinfectantes", icon: SprayCan },
    { id: "trajes", name: "Trajes de protección", icon: Shirt },
    { id: "accesorios", name: "Accesorios", icon: Package },
];