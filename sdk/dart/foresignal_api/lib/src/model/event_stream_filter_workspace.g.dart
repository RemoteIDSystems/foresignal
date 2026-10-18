// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_stream_filter_workspace.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStreamFilterWorkspaceCWProxy {
  EventStreamFilterWorkspace workspaceId(String workspaceId);

  EventStreamFilterWorkspace clientId(String? clientId);

  EventStreamFilterWorkspace clientKind(
    EventStreamFilterWorkspaceClientKindEnum? clientKind,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamFilterWorkspace(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamFilterWorkspace(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStreamFilterWorkspace call({
    String workspaceId,
    String? clientId,
    EventStreamFilterWorkspaceClientKindEnum? clientKind,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStreamFilterWorkspace.copyWith(...)` or call `instanceOfEventStreamFilterWorkspace.copyWith.fieldName(value)` for a single field.
class _$EventStreamFilterWorkspaceCWProxyImpl
    implements _$EventStreamFilterWorkspaceCWProxy {
  const _$EventStreamFilterWorkspaceCWProxyImpl(this._value);

  final EventStreamFilterWorkspace _value;

  @override
  EventStreamFilterWorkspace workspaceId(String workspaceId) =>
      call(workspaceId: workspaceId);

  @override
  EventStreamFilterWorkspace clientId(String? clientId) =>
      call(clientId: clientId);

  @override
  EventStreamFilterWorkspace clientKind(
    EventStreamFilterWorkspaceClientKindEnum? clientKind,
  ) => call(clientKind: clientKind);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamFilterWorkspace(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamFilterWorkspace(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStreamFilterWorkspace call({
    Object? workspaceId = const $CopyWithPlaceholder(),
    Object? clientId = const $CopyWithPlaceholder(),
    Object? clientKind = const $CopyWithPlaceholder(),
  }) {
    return EventStreamFilterWorkspace(
      workspaceId:
          workspaceId == const $CopyWithPlaceholder() || workspaceId == null
          ? _value.workspaceId
          // ignore: cast_nullable_to_non_nullable
          : workspaceId as String,
      clientId: clientId == const $CopyWithPlaceholder()
          ? _value.clientId
          // ignore: cast_nullable_to_non_nullable
          : clientId as String?,
      clientKind: clientKind == const $CopyWithPlaceholder()
          ? _value.clientKind
          // ignore: cast_nullable_to_non_nullable
          : clientKind as EventStreamFilterWorkspaceClientKindEnum?,
    );
  }
}

extension $EventStreamFilterWorkspaceCopyWith on EventStreamFilterWorkspace {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStreamFilterWorkspace.copyWith(...)` or `instanceOfEventStreamFilterWorkspace.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStreamFilterWorkspaceCWProxy get copyWith =>
      _$EventStreamFilterWorkspaceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStreamFilterWorkspace _$EventStreamFilterWorkspaceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStreamFilterWorkspace', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['workspaceId']);
  final val = EventStreamFilterWorkspace(
    workspaceId: $checkedConvert('workspaceId', (v) => v as String),
    clientId: $checkedConvert('clientId', (v) => v as String?),
    clientKind: $checkedConvert(
      'clientKind',
      (v) => $enumDecodeNullable(
        _$EventStreamFilterWorkspaceClientKindEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$EventStreamFilterWorkspaceToJson(
  EventStreamFilterWorkspace instance,
) => <String, dynamic>{
  'workspaceId': instance.workspaceId,
  'clientId': ?instance.clientId,
  'clientKind':
      ?_$EventStreamFilterWorkspaceClientKindEnumEnumMap[instance.clientKind],
};

const _$EventStreamFilterWorkspaceClientKindEnumEnumMap = {
  EventStreamFilterWorkspaceClientKindEnum.globe: 'globe',
  EventStreamFilterWorkspaceClientKindEnum.map: 'map',
  EventStreamFilterWorkspaceClientKindEnum.analyst: 'analyst',
  EventStreamFilterWorkspaceClientKindEnum.satellite: 'satellite',
  EventStreamFilterWorkspaceClientKindEnum.rf: 'rf',
  EventStreamFilterWorkspaceClientKindEnum.mobile: 'mobile',
  EventStreamFilterWorkspaceClientKindEnum.agent: 'agent',
  EventStreamFilterWorkspaceClientKindEnum.other: 'other',
};
