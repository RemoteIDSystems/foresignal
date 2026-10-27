from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    rule_id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "delete",
        "url": "/v1/triggers/{rule_id}".format(
            rule_id=quote(str(rule_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 204:
        response_204 = cast(Any, None)
        return response_204

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[Any | Problem]:
    """Delete a trigger rule

     Existing alerts are kept (no cascade).

    **Scope:** `agent`.

    **v2.0 (per-user, R2-008):** a rule is owned by the user who created it (`ownerPrincipalId`, that
    user's principal) and its alerts go to that user's channels. A signed-in user sees and manages only
    their own rules; another user's rule is 404, like an unknown one. A session holding `users.manage`
    and every API token see and manage all rules, the shared ones (`ownerPrincipalId: null`, the rules
    that existed before v2.0) included; a rule an API token creates is shared. Anonymous callers see
    shared rules only. This is a deliberate semantic change from the single-operator v1.x behavior; the
    shapes are unchanged except the additive, read-only `ownerPrincipalId`.

    Args:
        rule_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Any | Problem | None:
    """Delete a trigger rule

     Existing alerts are kept (no cascade).

    **Scope:** `agent`.

    **v2.0 (per-user, R2-008):** a rule is owned by the user who created it (`ownerPrincipalId`, that
    user's principal) and its alerts go to that user's channels. A signed-in user sees and manages only
    their own rules; another user's rule is 404, like an unknown one. A session holding `users.manage`
    and every API token see and manage all rules, the shared ones (`ownerPrincipalId: null`, the rules
    that existed before v2.0) included; a rule an API token creates is shared. Anonymous callers see
    shared rules only. This is a deliberate semantic change from the single-operator v1.x behavior; the
    shapes are unchanged except the additive, read-only `ownerPrincipalId`.

    Args:
        rule_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        rule_id=rule_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[Any | Problem]:
    """Delete a trigger rule

     Existing alerts are kept (no cascade).

    **Scope:** `agent`.

    **v2.0 (per-user, R2-008):** a rule is owned by the user who created it (`ownerPrincipalId`, that
    user's principal) and its alerts go to that user's channels. A signed-in user sees and manages only
    their own rules; another user's rule is 404, like an unknown one. A session holding `users.manage`
    and every API token see and manage all rules, the shared ones (`ownerPrincipalId: null`, the rules
    that existed before v2.0) included; a rule an API token creates is shared. Anonymous callers see
    shared rules only. This is a deliberate semantic change from the single-operator v1.x behavior; the
    shapes are unchanged except the additive, read-only `ownerPrincipalId`.

    Args:
        rule_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Any | Problem | None:
    """Delete a trigger rule

     Existing alerts are kept (no cascade).

    **Scope:** `agent`.

    **v2.0 (per-user, R2-008):** a rule is owned by the user who created it (`ownerPrincipalId`, that
    user's principal) and its alerts go to that user's channels. A signed-in user sees and manages only
    their own rules; another user's rule is 404, like an unknown one. A session holding `users.manage`
    and every API token see and manage all rules, the shared ones (`ownerPrincipalId: null`, the rules
    that existed before v2.0) included; a rule an API token creates is shared. Anonymous callers see
    shared rules only. This is a deliberate semantic change from the single-operator v1.x behavior; the
    shapes are unchanged except the additive, read-only `ownerPrincipalId`.

    Args:
        rule_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            rule_id=rule_id,
            client=client,
        )
    ).parsed
