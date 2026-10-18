// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'resync_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResyncEventCWProxy {
  ResyncEvent reason(ResyncEventReasonEnum reason);

  ResyncEvent watermarkSeq(int watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResyncEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResyncEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ResyncEvent call({ResyncEventReasonEnum reason, int watermarkSeq});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfResyncEvent.copyWith(...)` or call `instanceOfResyncEvent.copyWith.fieldName(value)` for a single field.
class _$ResyncEventCWProxyImpl implements _$ResyncEventCWProxy {
  const _$ResyncEventCWProxyImpl(this._value);

  final ResyncEvent _value;

  @override
  ResyncEvent reason(ResyncEventReasonEnum reason) => call(reason: reason);

  @override
  ResyncEvent watermarkSeq(int watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResyncEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResyncEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ResyncEvent call({
    Object? reason = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
  }) {
    return ResyncEvent(
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as ResyncEventReasonEnum,
      watermarkSeq:
          watermarkSeq == const $CopyWithPlaceholder() || watermarkSeq == null
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int,
    );
  }
}

extension $ResyncEventCopyWith on ResyncEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfResyncEvent.copyWith(...)` or `instanceOfResyncEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResyncEventCWProxy get copyWith => _$ResyncEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResyncEvent _$ResyncEventFromJson(Map<String, dynamic> json) => $checkedCreate(
  'ResyncEvent',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['reason', 'watermarkSeq']);
    final val = ResyncEvent(
      reason: $checkedConvert(
        'reason',
        (v) => $enumDecode(_$ResyncEventReasonEnumEnumMap, v),
      ),
      watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num).toInt()),
    );
    return val;
  },
);

Map<String, dynamic> _$ResyncEventToJson(ResyncEvent instance) =>
    <String, dynamic>{
      'reason': _$ResyncEventReasonEnumEnumMap[instance.reason]!,
      'watermarkSeq': instance.watermarkSeq,
    };

const _$ResyncEventReasonEnumEnumMap = {
  ResyncEventReasonEnum.gap: 'gap',
  ResyncEventReasonEnum.liveCap: 'live_cap',
  ResyncEventReasonEnum.liveDropped: 'live_dropped',
};
