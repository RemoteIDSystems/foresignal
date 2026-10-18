// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'history_frame_removals_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HistoryFrameRemovalsInnerCWProxy {
  HistoryFrameRemovalsInner id(String id);

  HistoryFrameRemovalsInner reason(HistoryFrameRemovalsInnerReasonEnum reason);

  HistoryFrameRemovalsInner left(bool left);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HistoryFrameRemovalsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HistoryFrameRemovalsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  HistoryFrameRemovalsInner call({
    String id,
    HistoryFrameRemovalsInnerReasonEnum reason,
    bool left,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHistoryFrameRemovalsInner.copyWith(...)` or call `instanceOfHistoryFrameRemovalsInner.copyWith.fieldName(value)` for a single field.
class _$HistoryFrameRemovalsInnerCWProxyImpl
    implements _$HistoryFrameRemovalsInnerCWProxy {
  const _$HistoryFrameRemovalsInnerCWProxyImpl(this._value);

  final HistoryFrameRemovalsInner _value;

  @override
  HistoryFrameRemovalsInner id(String id) => call(id: id);

  @override
  HistoryFrameRemovalsInner reason(
    HistoryFrameRemovalsInnerReasonEnum reason,
  ) => call(reason: reason);

  @override
  HistoryFrameRemovalsInner left(bool left) => call(left: left);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HistoryFrameRemovalsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HistoryFrameRemovalsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HistoryFrameRemovalsInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? left = const $CopyWithPlaceholder(),
  }) {
    return HistoryFrameRemovalsInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as HistoryFrameRemovalsInnerReasonEnum,
      left: left == const $CopyWithPlaceholder() || left == null
          ? _value.left
          // ignore: cast_nullable_to_non_nullable
          : left as bool,
    );
  }
}

extension $HistoryFrameRemovalsInnerCopyWith on HistoryFrameRemovalsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHistoryFrameRemovalsInner.copyWith(...)` or `instanceOfHistoryFrameRemovalsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HistoryFrameRemovalsInnerCWProxy get copyWith =>
      _$HistoryFrameRemovalsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HistoryFrameRemovalsInner _$HistoryFrameRemovalsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HistoryFrameRemovalsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'reason', 'left']);
  final val = HistoryFrameRemovalsInner(
    id: $checkedConvert('id', (v) => v as String),
    reason: $checkedConvert(
      'reason',
      (v) => $enumDecode(_$HistoryFrameRemovalsInnerReasonEnumEnumMap, v),
    ),
    left: $checkedConvert('left', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$HistoryFrameRemovalsInnerToJson(
  HistoryFrameRemovalsInner instance,
) => <String, dynamic>{
  'id': instance.id,
  'reason': _$HistoryFrameRemovalsInnerReasonEnumEnumMap[instance.reason]!,
  'left': instance.left,
};

const _$HistoryFrameRemovalsInnerReasonEnumEnumMap = {
  HistoryFrameRemovalsInnerReasonEnum.tombstone: 'tombstone',
  HistoryFrameRemovalsInnerReasonEnum.expired: 'expired',
  HistoryFrameRemovalsInnerReasonEnum.left: 'left',
};
