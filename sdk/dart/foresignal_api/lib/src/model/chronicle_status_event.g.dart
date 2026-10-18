// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_status_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleStatusEventCWProxy {
  ChronicleStatusEvent chronicleId(String chronicleId);

  ChronicleStatusEvent status(ChronicleStatusEventStatusEnum status);

  ChronicleStatusEvent previousStatus(
    ChronicleStatusEventPreviousStatusEnum? previousStatus,
  );

  ChronicleStatusEvent capState(ChronicleStatusEventCapStateEnum? capState);

  ChronicleStatusEvent bytesUsed(int? bytesUsed);

  ChronicleStatusEvent reason(String? reason);

  ChronicleStatusEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleStatusEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleStatusEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleStatusEvent call({
    String chronicleId,
    ChronicleStatusEventStatusEnum status,
    ChronicleStatusEventPreviousStatusEnum? previousStatus,
    ChronicleStatusEventCapStateEnum? capState,
    int? bytesUsed,
    String? reason,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleStatusEvent.copyWith(...)` or call `instanceOfChronicleStatusEvent.copyWith.fieldName(value)` for a single field.
class _$ChronicleStatusEventCWProxyImpl
    implements _$ChronicleStatusEventCWProxy {
  const _$ChronicleStatusEventCWProxyImpl(this._value);

  final ChronicleStatusEvent _value;

  @override
  ChronicleStatusEvent chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleStatusEvent status(ChronicleStatusEventStatusEnum status) =>
      call(status: status);

  @override
  ChronicleStatusEvent previousStatus(
    ChronicleStatusEventPreviousStatusEnum? previousStatus,
  ) => call(previousStatus: previousStatus);

  @override
  ChronicleStatusEvent capState(ChronicleStatusEventCapStateEnum? capState) =>
      call(capState: capState);

  @override
  ChronicleStatusEvent bytesUsed(int? bytesUsed) => call(bytesUsed: bytesUsed);

  @override
  ChronicleStatusEvent reason(String? reason) => call(reason: reason);

  @override
  ChronicleStatusEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleStatusEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleStatusEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleStatusEvent call({
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? previousStatus = const $CopyWithPlaceholder(),
    Object? capState = const $CopyWithPlaceholder(),
    Object? bytesUsed = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ChronicleStatusEvent(
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ChronicleStatusEventStatusEnum,
      previousStatus: previousStatus == const $CopyWithPlaceholder()
          ? _value.previousStatus
          // ignore: cast_nullable_to_non_nullable
          : previousStatus as ChronicleStatusEventPreviousStatusEnum?,
      capState: capState == const $CopyWithPlaceholder()
          ? _value.capState
          // ignore: cast_nullable_to_non_nullable
          : capState as ChronicleStatusEventCapStateEnum?,
      bytesUsed: bytesUsed == const $CopyWithPlaceholder()
          ? _value.bytesUsed
          // ignore: cast_nullable_to_non_nullable
          : bytesUsed as int?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ChronicleStatusEventCopyWith on ChronicleStatusEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleStatusEvent.copyWith(...)` or `instanceOfChronicleStatusEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleStatusEventCWProxy get copyWith =>
      _$ChronicleStatusEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleStatusEvent _$ChronicleStatusEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleStatusEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['chronicleId', 'status', 'at']);
  final val = ChronicleStatusEvent(
    chronicleId: $checkedConvert('chronicleId', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ChronicleStatusEventStatusEnumEnumMap, v),
    ),
    previousStatus: $checkedConvert(
      'previousStatus',
      (v) => $enumDecodeNullable(
        _$ChronicleStatusEventPreviousStatusEnumEnumMap,
        v,
      ),
    ),
    capState: $checkedConvert(
      'capState',
      (v) => $enumDecodeNullable(_$ChronicleStatusEventCapStateEnumEnumMap, v),
    ),
    bytesUsed: $checkedConvert('bytesUsed', (v) => (v as num?)?.toInt()),
    reason: $checkedConvert('reason', (v) => v as String?),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ChronicleStatusEventToJson(
  ChronicleStatusEvent instance,
) => <String, dynamic>{
  'chronicleId': instance.chronicleId,
  'status': _$ChronicleStatusEventStatusEnumEnumMap[instance.status]!,
  'previousStatus':
      ?_$ChronicleStatusEventPreviousStatusEnumEnumMap[instance.previousStatus],
  'capState': ?_$ChronicleStatusEventCapStateEnumEnumMap[instance.capState],
  'bytesUsed': ?instance.bytesUsed,
  'reason': ?instance.reason,
  'at': instance.at.toIso8601String(),
};

const _$ChronicleStatusEventStatusEnumEnumMap = {
  ChronicleStatusEventStatusEnum.proposed: 'proposed',
  ChronicleStatusEventStatusEnum.active: 'active',
  ChronicleStatusEventStatusEnum.paused: 'paused',
  ChronicleStatusEventStatusEnum.stopped: 'stopped',
  ChronicleStatusEventStatusEnum.archived: 'archived',
};

const _$ChronicleStatusEventPreviousStatusEnumEnumMap = {
  ChronicleStatusEventPreviousStatusEnum.proposed: 'proposed',
  ChronicleStatusEventPreviousStatusEnum.active: 'active',
  ChronicleStatusEventPreviousStatusEnum.paused: 'paused',
  ChronicleStatusEventPreviousStatusEnum.stopped: 'stopped',
  ChronicleStatusEventPreviousStatusEnum.archived: 'archived',
};

const _$ChronicleStatusEventCapStateEnumEnumMap = {
  ChronicleStatusEventCapStateEnum.ok: 'ok',
  ChronicleStatusEventCapStateEnum.warning: 'warning',
  ChronicleStatusEventCapStateEnum.exceeded: 'exceeded',
};
