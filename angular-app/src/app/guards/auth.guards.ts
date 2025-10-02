import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { inject } from '@angular/core';
import { AuthGuardData, createAuthGuard } from 'keycloak-angular';

/**
 * Access guard that does NOT directly use KeycloakService (by request).
 * It relies on the `AuthGuardData` provided by `createAuthGuard` and is defensive
 * about the shape of grantedRoles so it won't throw if Keycloak data is missing.
 */
const isAccessAllowed = async (
  route: ActivatedRouteSnapshot,
  _: RouterStateSnapshot,
  authData: AuthGuardData
): Promise<boolean | UrlTree> => {
  const { authenticated, grantedRoles } = authData;

  const requiredRole = route.data['role'];

  // If no role is required for this route, allow access.
  if (!requiredRole) {
    return true;
  }

  // Safely check realmRoles and resourceRoles for the required role.
  const hasRequiredRole = (role: string): boolean => {
    if (!grantedRoles) return false;

    // realmRoles: string[]
    if (Array.isArray((grantedRoles as any).realmRoles) && (grantedRoles as any).realmRoles.includes(role)) {
      return true;
    }

    // resourceRoles: Record<string, string[]>
    const resourceRoles = (grantedRoles as any).resourceRoles;
    if (resourceRoles && typeof resourceRoles === 'object') {
      return Object.values(resourceRoles).some((roles) => Array.isArray(roles) && roles.includes(role));
    }

    return false;
  };

  // Only allow if authenticated and has the required role.
  if (authenticated && hasRequiredRole(requiredRole)) {
    return true;
  }

  // Not allowed: redirect to /forbidden (or change to login flow if you prefer).
  const router = inject(Router);
  return router.parseUrl('/forbidden');
};

export const canActivateAuthRole = createAuthGuard<CanActivateFn>(isAccessAllowed);