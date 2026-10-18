from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_report_rendition_format import GetReportRenditionFormat
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    report_id: str,
    *,
    format_: GetReportRenditionFormat,
    revision: int | Unset = UNSET,
    draft: bool | Unset = False,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_format_ = format_.value
    params["format"] = json_format_

    params["revision"] = revision

    params["draft"] = draft

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/reports/{report_id}/rendition".format(
            report_id=quote(str(report_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | str | None:
    if response.status_code == 200:
        response_200 = response.text
        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

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
) -> Response[Problem | str]:
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
    format_: GetReportRenditionFormat,
    revision: int | Unset = UNSET,
    draft: bool | Unset = False,
) -> Response[Problem | str]:
    """Download a report rendition

     Markdown, HTML (TLP banner) or the JSON bundle of a published revision (default latest);
    `draft=true` renders the current draft for preview.

    **Scope:** `read`.

    Args:
        report_id (str):
        format_ (GetReportRenditionFormat):
        revision (int | Unset):
        draft (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | str]
    """

    kwargs = _get_kwargs(
        report_id=report_id,
        format_=format_,
        revision=revision,
        draft=draft,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    report_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetReportRenditionFormat,
    revision: int | Unset = UNSET,
    draft: bool | Unset = False,
) -> Problem | str | None:
    """Download a report rendition

     Markdown, HTML (TLP banner) or the JSON bundle of a published revision (default latest);
    `draft=true` renders the current draft for preview.

    **Scope:** `read`.

    Args:
        report_id (str):
        format_ (GetReportRenditionFormat):
        revision (int | Unset):
        draft (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | str
    """

    return sync_detailed(
        report_id=report_id,
        client=client,
        format_=format_,
        revision=revision,
        draft=draft,
    ).parsed


async def asyncio_detailed(
    report_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetReportRenditionFormat,
    revision: int | Unset = UNSET,
    draft: bool | Unset = False,
) -> Response[Problem | str]:
    """Download a report rendition

     Markdown, HTML (TLP banner) or the JSON bundle of a published revision (default latest);
    `draft=true` renders the current draft for preview.

    **Scope:** `read`.

    Args:
        report_id (str):
        format_ (GetReportRenditionFormat):
        revision (int | Unset):
        draft (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | str]
    """

    kwargs = _get_kwargs(
        report_id=report_id,
        format_=format_,
        revision=revision,
        draft=draft,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    report_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetReportRenditionFormat,
    revision: int | Unset = UNSET,
    draft: bool | Unset = False,
) -> Problem | str | None:
    """Download a report rendition

     Markdown, HTML (TLP banner) or the JSON bundle of a published revision (default latest);
    `draft=true` renders the current draft for preview.

    **Scope:** `read`.

    Args:
        report_id (str):
        format_ (GetReportRenditionFormat):
        revision (int | Unset):
        draft (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | str
    """

    return (
        await asyncio_detailed(
            report_id=report_id,
            client=client,
            format_=format_,
            revision=revision,
            draft=draft,
        )
    ).parsed
