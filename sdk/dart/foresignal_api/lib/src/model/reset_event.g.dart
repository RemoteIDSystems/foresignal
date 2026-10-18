// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reset_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResetEventCWProxy {
  ResetEvent reason(ResetEventReasonEnum reason);

  ResetEvent resumeAfter(int? resumeAfter);

  ResetEvent watermarkSeq(int? watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResetEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResetEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ResetEvent call({
    ResetEventReasonEnum reason,
    int? resumeAfter,
    int? watermarkSeq,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfResetEvent.copyWith(...)` or call `instanceOfResetEvent.copyWith.fieldName(value)` for a single field.
class _$ResetEventCWProxyImpl implements _$ResetEventCWProxy {
  const _$ResetEventCWProxyImpl(this._value);

  final ResetEvent _value;

  @override
  ResetEvent reason(ResetEventReasonEnum reason) => call(reason: reason);

  @override
  ResetEvent resumeAfter(int? resumeAfter) => call(resumeAfter: resumeAfter);

  @override
  ResetEvent watermarkSeq(int? watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResetEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResetEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ResetEvent call({
    Object? reason = const $CopyWithPlaceholder(),
    Object? resumeAfter = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
  }) {
    return ResetEvent(
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as ResetEventReasonEnum,
      resumeAfter: resumeAfter == const $CopyWithPlaceholder()
          ? _value.resumeAfter
          // ignore: cast_nullable_to_non_nullable
          : resumeAfter as int?,
      watermarkSeq: watermarkSeq == const $CopyWithPlaceholder()
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int?,
    );
  }
}

extension $ResetEventCopyWith on ResetEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfResetEvent.copyWith(...)` or `instanceOfResetEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResetEventCWProxy get copyWith => _$ResetEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResetEvent _$ResetEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ResetEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['reason']);
  final val = ResetEvent(
    reason: $checkedConvert(
      'reason',
      (v) => $enumDecode(_$ResetEventReasonEnumEnumMap, v),
    ),
    resumeAfter: $checkedConvert('resumeAfter', (v) => (v as num?)?.toInt()),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$ResetEventToJson(ResetEvent instance) =>
    <String, dynamic>{
      'reason': _$ResetEventReasonEnumEnumMap[instance.reason]!,
      'resumeAfter': ?instance.resumeAfter,
      'watermarkSeq': ?instance.watermarkSeq,
    };

const _$ResetEventReasonEnumEnumMap = {
  ResetEventReasonEnum.bufferOverflow: 'buffer_overflow',
  ResetEventReasonEnum.replayCap: 'replay_cap',
  ResetEventReasonEnum.resyncRequired: 'resync_required',
};
