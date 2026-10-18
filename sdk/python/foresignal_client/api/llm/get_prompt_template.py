from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.prompt_template import PromptTemplate
from ...types import UNSET, Response, Unset


def _get_kwargs(
    template_id: str,
    *,
    version: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["version"] = version

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/prompt-templates/{template_id}".format(
            template_id=quote(str(template_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | PromptTemplate | None:
    if response.status_code == 200:
        response_200 = PromptTemplate.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | PromptTemplate]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    template_id: str,
    *,
    client: AuthenticatedClient,
    version: str | Unset = UNSET,
) -> Response[Problem | PromptTemplate]:
    """Get an LLM prompt template

     The versioned system prompt and instructions of an LLM role, as recorded by manifests ({templateId,
    version}). Operator `instructions` are returned to agent scope only; filled-in prompts are not
    exposed here.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        template_id (str):
        version (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PromptTemplate]
    """

    kwargs = _get_kwargs(
        template_id=template_id,
        version=version,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    template_id: str,
    *,
    client: AuthenticatedClient,
    version: str | Unset = UNSET,
) -> Problem | PromptTemplate | None:
    """Get an LLM prompt template

     The versioned system prompt and instructions of an LLM role, as recorded by manifests ({templateId,
    version}). Operator `instructions` are returned to agent scope only; filled-in prompts are not
    exposed here.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        template_id (str):
        version (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PromptTemplate
    """

    return sync_detailed(
        template_id=template_id,
        client=client,
        version=version,
    ).parsed


async def asyncio_detailed(
    template_id: str,
    *,
    client: AuthenticatedClient,
    version: str | Unset = UNSET,
) -> Response[Problem | PromptTemplate]:
    """Get an LLM prompt template

     The versioned system prompt and instructions of an LLM role, as recorded by manifests ({templateId,
    version}). Operator `instructions` are returned to agent scope only; filled-in prompts are not
    exposed here.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        template_id (str):
        version (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PromptTemplate]
    """

    kwargs = _get_kwargs(
        template_id=template_id,
        version=version,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    template_id: str,
    *,
    client: AuthenticatedClient,
    version: str | Unset = UNSET,
) -> Problem | PromptTemplate | None:
    """Get an LLM prompt template

     The versioned system prompt and instructions of an LLM role, as recorded by manifests ({templateId,
    version}). Operator `instructions` are returned to agent scope only; filled-in prompts are not
    exposed here.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        template_id (str):
        version (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PromptTemplate
    """

    return (
        await asyncio_detailed(
            template_id=template_id,
            client=client,
            version=version,
        )
    ).parsed
