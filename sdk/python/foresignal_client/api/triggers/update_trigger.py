from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.trigger_rule import TriggerRule
from ...models.trigger_rule_input import TriggerRuleInput
from ...types import Response


def _get_kwargs(
    rule_id: UUID,
    *,
    body: TriggerRuleInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/triggers/{rule_id}".format(
            rule_id=quote(str(rule_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TriggerRule | None:
    if response.status_code == 200:
        response_200 = TriggerRule.from_dict(response.json())

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
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    body: TriggerRuleInput,
) -> Response[Problem | TriggerRule]:
    """Replace a trigger rule

     Full replace; the persist gate re-converges each entity on its next observation.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        body (TriggerRuleInput): All conditions are ANDed. At least one of sources, kinds,
            conditions, geofence or near is required. Unknown sources → 400; unknown kinds warn.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TriggerRule]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    body: TriggerRuleInput,
) -> Problem | TriggerRule | None:
    """Replace a trigger rule

     Full replace; the persist gate re-converges each entity on its next observation.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        body (TriggerRuleInput): All conditions are ANDed. At least one of sources, kinds,
            conditions, geofence or near is required. Unknown sources → 400; unknown kinds warn.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TriggerRule
    """

    return sync_detailed(
        rule_id=rule_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    body: TriggerRuleInput,
) -> Response[Problem | TriggerRule]:
    """Replace a trigger rule

     Full replace; the persist gate re-converges each entity on its next observation.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        body (TriggerRuleInput): All conditions are ANDed. At least one of sources, kinds,
            conditions, geofence or near is required. Unknown sources → 400; unknown kinds warn.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TriggerRule]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    body: TriggerRuleInput,
) -> Problem | TriggerRule | None:
    """Replace a trigger rule

     Full replace; the persist gate re-converges each entity on its next observation.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
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
            rule_id=rule_id,
            client=client,
            body=body,
        )
    ).parsed
