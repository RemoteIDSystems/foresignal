// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_source_state_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalSourceStateEventCWProxy {
  ExternalSourceStateEvent sourceId(String sourceId);

  ExternalSourceStateEvent state(String? state);

  ExternalSourceStateEvent change(ExternalSourceStateEventChangeEnum change);

  ExternalSourceStateEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSourceStateEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSourceStateEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalSourceStateEvent call({
    String sourceId,
    String? state,
    ExternalSourceStateEventChangeEnum change,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalSourceStateEvent.copyWith(...)` or call `instanceOfExternalSourceStateEvent.copyWith.fieldName(value)` for a single field.
class _$ExternalSourceStateEventCWProxyImpl
    implements _$ExternalSourceStateEventCWProxy {
  const _$ExternalSourceStateEventCWProxyImpl(this._value);

  final ExternalSourceStateEvent _value;

  @override
  ExternalSourceStateEvent sourceId(String sourceId) =>
      call(sourceId: sourceId);

  @override
  ExternalSourceStateEvent state(String? state) => call(state: state);

  @override
  ExternalSourceStateEvent change(ExternalSourceStateEventChangeEnum change) =>
      call(change: change);

  @override
  ExternalSourceStateEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSourceStateEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSourceStateEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalSourceStateEvent call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? change = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ExternalSourceStateEvent(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      state: state == const $CopyWithPlaceholder()
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as String?,
      change: change == const $CopyWithPlaceholder() || change == null
          ? _value.change
          // ignore: cast_nullable_to_non_nullable
          : change as ExternalSourceStateEventChangeEnum,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $ExternalSourceStateEventCopyWith on ExternalSourceStateEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalSourceStateEvent.copyWith(...)` or `instanceOfExternalSourceStateEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalSourceStateEventCWProxy get copyWith =>
      _$ExternalSourceStateEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalSourceStateEvent _$ExternalSourceStateEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalSourceStateEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['sourceId', 'change', 'at']);
  final val = ExternalSourceStateEvent(
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    state: $checkedConvert('state', (v) => v as String?),
    change: $checkedConvert(
      'change',
      (v) => $enumDecode(_$ExternalSourceStateEventChangeEnumEnumMap, v),
    ),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$ExternalSourceStateEventToJson(
  ExternalSourceStateEvent instance,
) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'state': ?instance.state,
  'change': _$ExternalSourceStateEventChangeEnumEnumMap[instance.change]!,
  'at': instance.at.toIso8601String(),
};

const _$ExternalSourceStateEventChangeEnumEnumMap = {
  ExternalSourceStateEventChangeEnum.registered: 'registered',
  ExternalSourceStateEventChangeEnum.approved: 'approved',
  ExternalSourceStateEventChangeEnum.suspended: 'suspended',
  ExternalSourceStateEventChangeEnum.manifestChanged: 'manifest-changed',
  ExternalSourceStateEventChangeEnum.deleted: 'deleted',
};
