from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.action_result import ActionResult
from ...models.execute_action_data_body import ExecuteActionDataBody
from ...models.execute_action_json_body import ExecuteActionJsonBody
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    token: str,
    *,
    body: ExecuteActionDataBody | ExecuteActionJsonBody | Unset = UNSET,
    origin: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(origin, Unset):
        headers["Origin"] = origin

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/actions/{token}".format(
            token=quote(str(token), safe=""),
        ),
    }

    if isinstance(body, ExecuteActionDataBody):
        if not isinstance(body, Unset):
            _kwargs["data"] = body.to_dict()
        headers["Content-Type"] = "application/x-www-form-urlencoded"
    if isinstance(body, ExecuteActionJsonBody):
        if not isinstance(body, Unset):
            _kwargs["json"] = body.to_dict()

        headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ActionResult | Problem | None:
    if response.status_code == 200:
        response_200 = ActionResult.from_dict(response.json())

        return response_200

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ActionResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    token: str,
    *,
    client: AuthenticatedClient,
    body: ExecuteActionDataBody | ExecuteActionJsonBody | Unset = UNSET,
    origin: str | Unset = UNSET,
) -> Response[ActionResult | Problem]:
    """Execute a notification action

     Executes the capability once (single use, in one transaction): ack sets
    `acknowledged_by=action:<channelType>:<channelName>`, snooze snoozes the alert, mute mutes the rule.
    An already acknowledged or resolved alert returns 200 with `result: already-<state>`. `Accept:
    application/json` → JSON, otherwise an HTML result page. An `Origin` other than the `publicBaseUrl`
    origin or the request's own origin (scheme and Host, behind trusted proxies) → 403. Audited; 30
    requests/min per client IP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        token (str):
        origin (str | Unset):
        body (ExecuteActionDataBody | Unset): Confirm-page form post (no fields required)
        body (ExecuteActionJsonBody | Unset): Empty JSON body from service workers or apps

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ActionResult | Problem]
    """

    kwargs = _get_kwargs(
        token=token,
        body=body,
        origin=origin,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    token: str,
    *,
    client: AuthenticatedClient,
    body: ExecuteActionDataBody | ExecuteActionJsonBody | Unset = UNSET,
    origin: str | Unset = UNSET,
) -> ActionResult | Problem | None:
    """Execute a notification action

     Executes the capability once (single use, in one transaction): ack sets
    `acknowledged_by=action:<channelType>:<channelName>`, snooze snoozes the alert, mute mutes the rule.
    An already acknowledged or resolved alert returns 200 with `result: already-<state>`. `Accept:
    application/json` → JSON, otherwise an HTML result page. An `Origin` other than the `publicBaseUrl`
    origin or the request's own origin (scheme and Host, behind trusted proxies) → 403. Audited; 30
    requests/min per client IP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        token (str):
        origin (str | Unset):
        body (ExecuteActionDataBody | Unset): Confirm-page form post (no fields required)
        body (ExecuteActionJsonBody | Unset): Empty JSON body from service workers or apps

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ActionResult | Problem
    """

    return sync_detailed(
        token=token,
        client=client,
        body=body,
        origin=origin,
    ).parsed


async def asyncio_detailed(
    token: str,
    *,
    client: AuthenticatedClient,
    body: ExecuteActionDataBody | ExecuteActionJsonBody | Unset = UNSET,
    origin: str | Unset = UNSET,
) -> Response[ActionResult | Problem]:
    """Execute a notification action

     Executes the capability once (single use, in one transaction): ack sets
    `acknowledged_by=action:<channelType>:<channelName>`, snooze snoozes the alert, mute mutes the rule.
    An already acknowledged or resolved alert returns 200 with `result: already-<state>`. `Accept:
    application/json` → JSON, otherwise an HTML result page. An `Origin` other than the `publicBaseUrl`
    origin or the request's own origin (scheme and Host, behind trusted proxies) → 403. Audited; 30
    requests/min per client IP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        token (str):
        origin (str | Unset):
        body (ExecuteActionDataBody | Unset): Confirm-page form post (no fields required)
        body (ExecuteActionJsonBody | Unset): Empty JSON body from service workers or apps

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ActionResult | Problem]
    """

    kwargs = _get_kwargs(
        token=token,
        body=body,
        origin=origin,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    token: str,
    *,
    client: AuthenticatedClient,
    body: ExecuteActionDataBody | ExecuteActionJsonBody | Unset = UNSET,
    origin: str | Unset = UNSET,
) -> ActionResult | Problem | None:
    """Execute a notification action

     Executes the capability once (single use, in one transaction): ack sets
    `acknowledged_by=action:<channelType>:<channelName>`, snooze snoozes the alert, mute mutes the rule.
    An already acknowledged or resolved alert returns 200 with `result: already-<state>`. `Accept:
    application/json` → JSON, otherwise an HTML result page. An `Origin` other than the `publicBaseUrl`
    origin or the request's own origin (scheme and Host, behind trusted proxies) → 403. Audited; 30
    requests/min per client IP.

    **Scope:** open (no token; the credential is in the request).

    Args:
        token (str):
        origin (str | Unset):
        body (ExecuteActionDataBody | Unset): Confirm-page form post (no fields required)
        body (ExecuteActionJsonBody | Unset): Empty JSON body from service workers or apps

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ActionResult | Problem
    """

    return (
        await asyncio_detailed(
            token=token,
            client=client,
            body=body,
            origin=origin,
        )
    ).parsed
