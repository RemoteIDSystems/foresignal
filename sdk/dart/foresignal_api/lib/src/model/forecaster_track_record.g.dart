// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecaster_track_record.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecasterTrackRecordCWProxy {
  ForecasterTrackRecord nResolved(int nResolved);

  ForecasterTrackRecord brier(num? brier);

  ForecasterTrackRecord label(ForecasterTrackRecordLabelEnum label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecasterTrackRecord(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecasterTrackRecord(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecasterTrackRecord call({
    int nResolved,
    num? brier,
    ForecasterTrackRecordLabelEnum label,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecasterTrackRecord.copyWith(...)` or call `instanceOfForecasterTrackRecord.copyWith.fieldName(value)` for a single field.
class _$ForecasterTrackRecordCWProxyImpl
    implements _$ForecasterTrackRecordCWProxy {
  const _$ForecasterTrackRecordCWProxyImpl(this._value);

  final ForecasterTrackRecord _value;

  @override
  ForecasterTrackRecord nResolved(int nResolved) => call(nResolved: nResolved);

  @override
  ForecasterTrackRecord brier(num? brier) => call(brier: brier);

  @override
  ForecasterTrackRecord label(ForecasterTrackRecordLabelEnum label) =>
      call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecasterTrackRecord(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecasterTrackRecord(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecasterTrackRecord call({
    Object? nResolved = const $CopyWithPlaceholder(),
    Object? brier = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return ForecasterTrackRecord(
      nResolved: nResolved == const $CopyWithPlaceholder() || nResolved == null
          ? _value.nResolved
          // ignore: cast_nullable_to_non_nullable
          : nResolved as int,
      brier: brier == const $CopyWithPlaceholder()
          ? _value.brier
          // ignore: cast_nullable_to_non_nullable
          : brier as num?,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as ForecasterTrackRecordLabelEnum,
    );
  }
}

extension $ForecasterTrackRecordCopyWith on ForecasterTrackRecord {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecasterTrackRecord.copyWith(...)` or `instanceOfForecasterTrackRecord.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecasterTrackRecordCWProxy get copyWith =>
      _$ForecasterTrackRecordCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecasterTrackRecord _$ForecasterTrackRecordFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecasterTrackRecord', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['nResolved', 'label']);
  final val = ForecasterTrackRecord(
    nResolved: $checkedConvert('nResolved', (v) => (v as num).toInt()),
    brier: $checkedConvert('brier', (v) => v as num?),
    label: $checkedConvert(
      'label',
      (v) => $enumDecode(_$ForecasterTrackRecordLabelEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$ForecasterTrackRecordToJson(
  ForecasterTrackRecord instance,
) => <String, dynamic>{
  'nResolved': instance.nResolved,
  'brier': ?instance.brier,
  'label': _$ForecasterTrackRecordLabelEnumEnumMap[instance.label]!,
};

const _$ForecasterTrackRecordLabelEnumEnumMap = {
  ForecasterTrackRecordLabelEnum.ok: 'ok',
  ForecasterTrackRecordLabelEnum.insufficient: 'insufficient',
};
