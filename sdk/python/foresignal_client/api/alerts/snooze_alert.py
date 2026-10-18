from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.alert import Alert
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    alert_id: int,
    *,
    body: Any,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/alerts/{alert_id}/snooze".format(
            alert_id=quote(str(alert_id), safe=""),
        ),
    }

    _kwargs["json"] = body

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Alert | Problem | None:
    if response.status_code == 200:
        response_200 = Alert.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Alert | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    alert_id: int,
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Response[Alert | Problem]:
    """Snooze an alert

     Until the expiry, new matches of the same rule and entity are recorded as suppressed
    (`alert_snoozed`) and not routed. Publishes `alert.updated`.

    **Scope:** `agent`.

    Args:
        alert_id (int):
        body (Any): Snooze until a time or for a duration (exactly one; required expiry; rule
            snoozes ≤ 7 days)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Alert | Problem]
    """

    kwargs = _get_kwargs(
        alert_id=alert_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    alert_id: int,
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Alert | Problem | None:
    """Snooze an alert

     Until the expiry, new matches of the same rule and entity are recorded as suppressed
    (`alert_snoozed`) and not routed. Publishes `alert.updated`.

    **Scope:** `agent`.

    Args:
        alert_id (int):
        body (Any): Snooze until a time or for a duration (exactly one; required expiry; rule
            snoozes ≤ 7 days)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Alert | Problem
    """

    return sync_detailed(
        alert_id=alert_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    alert_id: int,
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Response[Alert | Problem]:
    """Snooze an alert

     Until the expiry, new matches of the same rule and entity are recorded as suppressed
    (`alert_snoozed`) and not routed. Publishes `alert.updated`.

    **Scope:** `agent`.

    Args:
        alert_id (int):
        body (Any): Snooze until a time or for a duration (exactly one; required expiry; rule
            snoozes ≤ 7 days)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Alert | Problem]
    """

    kwargs = _get_kwargs(
        alert_id=alert_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    alert_id: int,
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Alert | Problem | None:
    """Snooze an alert

     Until the expiry, new matches of the same rule and entity are recorded as suppressed
    (`alert_snoozed`) and not routed. Publishes `alert.updated`.

    **Scope:** `agent`.

    Args:
        alert_id (int):
        body (Any): Snooze until a time or for a duration (exactly one; required expiry; rule
            snoozes ≤ 7 days)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Alert | Problem
    """

    return (
        await asyncio_detailed(
            alert_id=alert_id,
            client=client,
            body=body,
        )
    ).parsed
