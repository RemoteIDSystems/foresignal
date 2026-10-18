from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.trigger_rule import TriggerRule
from ...models.trigger_rule_input import TriggerRuleInput
from ...types import Response


def _get_kwargs(
    *,
    body: TriggerRuleInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/triggers",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TriggerRule | None:
    if response.status_code == 201:
        response_201 = TriggerRule.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | TriggerRule]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: TriggerRuleInput,
) -> Response[Problem | TriggerRule]:
    """Create a trigger rule

     Creates a rule evaluated against persisted versions (HEARTBEAT excluded). Condition paths on
    volatile fields are rejected (TRIGGER_PATH_VOLATILE); line geofences need bufferMeters;
    `near.placeId` needs Places (PLACES_UNAVAILABLE).

    **Scope:** `agent`.

    Args:
        body (TriggerRuleInput): All conditions are ANDed. At least one of sources, kinds,
            conditions, geofence or near is required. Unknown sources → 400; unknown kinds warn.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TriggerRule]
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
    body: TriggerRuleInput,
) -> Problem | TriggerRule | None:
    """Create a trigger rule

     Creates a rule evaluated against persisted versions (HEARTBEAT excluded). Condition paths on
    volatile fields are rejected (TRIGGER_PATH_VOLATILE); line geofences need bufferMeters;
    `near.placeId` needs Places (PLACES_UNAVAILABLE).

    **Scope:** `agent`.

    Args:
        body (TriggerRuleInput): All conditions are ANDed. At least one of sources, kinds,
            conditions, geofence or near is required. Unknown sources → 400; unknown kinds warn.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TriggerRule
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: TriggerRuleInput,
) -> Response[Problem | TriggerRule]:
    """Create a trigger rule

     Creates a rule evaluated against persisted versions (HEARTBEAT excluded). Condition paths on
    volatile fields are rejected (TRIGGER_PATH_VOLATILE); line geofences need bufferMeters;
    `near.placeId` needs Places (PLACES_UNAVAILABLE).

    **Scope:** `agent`.

    Args:
        body (TriggerRuleInput): All conditions are ANDed. At least one of sources, kinds,
            conditions, geofence or near is required. Unknown sources → 400; unknown kinds warn.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TriggerRule]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: TriggerRuleInput,
) -> Problem | TriggerRule | None:
    """Create a trigger rule

     Creates a rule evaluated against persisted versions (HEARTBEAT excluded). Condition paths on
    volatile fields are rejected (TRIGGER_PATH_VOLATILE); line geofences need bufferMeters;
    `near.placeId` needs Places (PLACES_UNAVAILABLE).

    **Scope:** `agent`.

    Args:
        body (TriggerRuleInput): All conditions are ANDed. At least one of sources, kinds,
            conditions, geofence or near is required. Unknown sources → 400; unknown kinds warn.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TriggerRule
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
