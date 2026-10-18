from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.report_send import ReportSend
from ...models.report_send_input import ReportSendInput
from ...types import Response


def _get_kwargs(
    report_id: str,
    *,
    body: ReportSendInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/reports/{report_id}/send".format(
            report_id=quote(str(report_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ReportSend | None:
    if response.status_code == 201:
        response_201 = ReportSend.from_dict(response.json())

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
) -> Response[Problem | ReportSend]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportSendInput,
) -> Response[Problem | ReportSend]:
    """Send a report on this instance

     Grants viewer access to principals and groups, notifies them (130 preferences; inbox item; optional
    HTML or link via their channels) and records the send. TLP is checked per recipient.

    **Scope:** `agent`.

    Args:
        report_id (str):
        body (ReportSendInput): Send a report to principals and groups on this instance

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReportSend]
    """

    kwargs = _get_kwargs(
        report_id=report_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportSendInput,
) -> Problem | ReportSend | None:
    """Send a report on this instance

     Grants viewer access to principals and groups, notifies them (130 preferences; inbox item; optional
    HTML or link via their channels) and records the send. TLP is checked per recipient.

    **Scope:** `agent`.

    Args:
        report_id (str):
        body (ReportSendInput): Send a report to principals and groups on this instance

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReportSend
    """

    return sync_detailed(
        report_id=report_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportSendInput,
) -> Response[Problem | ReportSend]:
    """Send a report on this instance

     Grants viewer access to principals and groups, notifies them (130 preferences; inbox item; optional
    HTML or link via their channels) and records the send. TLP is checked per recipient.

    **Scope:** `agent`.

    Args:
        report_id (str):
        body (ReportSendInput): Send a report to principals and groups on this instance

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReportSend]
    """

    kwargs = _get_kwargs(
        report_id=report_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportSendInput,
) -> Problem | ReportSend | None:
    """Send a report on this instance

     Grants viewer access to principals and groups, notifies them (130 preferences; inbox item; optional
    HTML or link via their channels) and records the send. TLP is checked per recipient.

    **Scope:** `agent`.

    Args:
        report_id (str):
        body (ReportSendInput): Send a report to principals and groups on this instance

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReportSend
    """

    return (
        await asyncio_detailed(
            report_id=report_id,
            client=client,
            body=body,
        )
    ).parsed
