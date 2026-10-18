from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.api_token_created import ApiTokenCreated
from ...models.api_token_input import ApiTokenInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ApiTokenInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/tokens",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ApiTokenCreated | Problem | None:
    if response.status_code == 201:
        response_201 = ApiTokenCreated.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ApiTokenCreated | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ApiTokenInput,
) -> Response[ApiTokenCreated | Problem]:
    """Create an API token

     Returns the token value (`fsr_` + 32 random bytes, base64url) exactly once. Optional `restrictions`
    (`ReadRestriction`: sources, kinds, bbox, area, endpoints, excludePrivate, maxRequestsPerMinute) are
    validated: endpoint patterns must match an operation the scope can reach, source ids and kinds must
    exist; bbox within ±180/±90 with min < max; area a valid GeoJSON Polygon or MultiPolygon of at most
    2,000 vertices; maxRequestsPerMinute at least 1. Data restrictions are applied on every read path
    (035). Members later prompts enforce (`placeIds` from 036, `federation`, `includeExercise` from 113)
    and `privateAccess: true` (039) are refused with 400 until then. Restrictions on admin tokens → 400
    RESTRICTION_NOT_ALLOWED (035).

    **Scope:** `admin`.

    Args:
        body (ApiTokenInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ApiTokenCreated | Problem]
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
    body: ApiTokenInput,
) -> ApiTokenCreated | Problem | None:
    """Create an API token

     Returns the token value (`fsr_` + 32 random bytes, base64url) exactly once. Optional `restrictions`
    (`ReadRestriction`: sources, kinds, bbox, area, endpoints, excludePrivate, maxRequestsPerMinute) are
    validated: endpoint patterns must match an operation the scope can reach, source ids and kinds must
    exist; bbox within ±180/±90 with min < max; area a valid GeoJSON Polygon or MultiPolygon of at most
    2,000 vertices; maxRequestsPerMinute at least 1. Data restrictions are applied on every read path
    (035). Members later prompts enforce (`placeIds` from 036, `federation`, `includeExercise` from 113)
    and `privateAccess: true` (039) are refused with 400 until then. Restrictions on admin tokens → 400
    RESTRICTION_NOT_ALLOWED (035).

    **Scope:** `admin`.

    Args:
        body (ApiTokenInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ApiTokenCreated | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ApiTokenInput,
) -> Response[ApiTokenCreated | Problem]:
    """Create an API token

     Returns the token value (`fsr_` + 32 random bytes, base64url) exactly once. Optional `restrictions`
    (`ReadRestriction`: sources, kinds, bbox, area, endpoints, excludePrivate, maxRequestsPerMinute) are
    validated: endpoint patterns must match an operation the scope can reach, source ids and kinds must
    exist; bbox within ±180/±90 with min < max; area a valid GeoJSON Polygon or MultiPolygon of at most
    2,000 vertices; maxRequestsPerMinute at least 1. Data restrictions are applied on every read path
    (035). Members later prompts enforce (`placeIds` from 036, `federation`, `includeExercise` from 113)
    and `privateAccess: true` (039) are refused with 400 until then. Restrictions on admin tokens → 400
    RESTRICTION_NOT_ALLOWED (035).

    **Scope:** `admin`.

    Args:
        body (ApiTokenInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ApiTokenCreated | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ApiTokenInput,
) -> ApiTokenCreated | Problem | None:
    """Create an API token

     Returns the token value (`fsr_` + 32 random bytes, base64url) exactly once. Optional `restrictions`
    (`ReadRestriction`: sources, kinds, bbox, area, endpoints, excludePrivate, maxRequestsPerMinute) are
    validated: endpoint patterns must match an operation the scope can reach, source ids and kinds must
    exist; bbox within ±180/±90 with min < max; area a valid GeoJSON Polygon or MultiPolygon of at most
    2,000 vertices; maxRequestsPerMinute at least 1. Data restrictions are applied on every read path
    (035). Members later prompts enforce (`placeIds` from 036, `federation`, `includeExercise` from 113)
    and `privateAccess: true` (039) are refused with 400 until then. Restrictions on admin tokens → 400
    RESTRICTION_NOT_ALLOWED (035).

    **Scope:** `admin`.

    Args:
        body (ApiTokenInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ApiTokenCreated | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
