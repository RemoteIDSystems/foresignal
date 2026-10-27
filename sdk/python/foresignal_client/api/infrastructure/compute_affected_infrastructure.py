from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.infra_impact_request import InfraImpactRequest
from ...models.infra_impact_result import InfraImpactResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: InfraImpactRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/infra/affected",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> InfraImpactResult | Problem | None:
    if response.status_code == 200:
        response_200 = InfraImpactResult.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[InfraImpactResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: InfraImpactRequest,
) -> Response[InfraImpactResult | Problem]:
    """Facilities affected by an area

     Seeds from an area, event or record and walks dependency edges (depth ≤ 3, label ≥ minLabel).
    Results ≤ 5,000 nodes (truncated by pathScore). An area > 2,000,000 km² without `types` is 400
    INFRA_QUERY_TOO_LARGE.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        body (InfraImpactRequest): Affected-dependents query: exactly one of geometry, eventId or
            record

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InfraImpactResult | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: InfraImpactRequest,
) -> InfraImpactResult | Problem | None:
    """Facilities affected by an area

     Seeds from an area, event or record and walks dependency edges (depth ≤ 3, label ≥ minLabel).
    Results ≤ 5,000 nodes (truncated by pathScore). An area > 2,000,000 km² without `types` is 400
    INFRA_QUERY_TOO_LARGE.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        body (InfraImpactRequest): Affected-dependents query: exactly one of geometry, eventId or
            record

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InfraImpactResult | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: InfraImpactRequest,
) -> Response[InfraImpactResult | Problem]:
    """Facilities affected by an area

     Seeds from an area, event or record and walks dependency edges (depth ≤ 3, label ≥ minLabel).
    Results ≤ 5,000 nodes (truncated by pathScore). An area > 2,000,000 km² without `types` is 400
    INFRA_QUERY_TOO_LARGE.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        body (InfraImpactRequest): Affected-dependents query: exactly one of geometry, eventId or
            record

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InfraImpactResult | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: InfraImpactRequest,
) -> InfraImpactResult | Problem | None:
    """Facilities affected by an area

     Seeds from an area, event or record and walks dependency edges (depth ≤ 3, label ≥ minLabel).
    Results ≤ 5,000 nodes (truncated by pathScore). An area > 2,000,000 km² without `types` is 400
    INFRA_QUERY_TOO_LARGE.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        body (InfraImpactRequest): Affected-dependents query: exactly one of geometry, eventId or
            record

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InfraImpactResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
