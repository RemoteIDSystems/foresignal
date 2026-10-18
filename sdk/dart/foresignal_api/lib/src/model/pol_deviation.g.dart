// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_deviation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolDeviationCWProxy {
  PolDeviation subjectId(String subjectId);

  PolDeviation subjectRef(String? subjectRef);

  PolDeviation deviationType(PolDeviationDeviationTypeEnum deviationType);

  PolDeviation at(DateTime at);

  PolDeviation placeId(String? placeId);

  PolDeviation placeLabel(String? placeLabel);

  PolDeviation stay(PolDeviationStay? stay);

  PolDeviation hourOfWeek(int? hourOfWeek);

  PolDeviation bucketShare(num? bucketShare);

  PolDeviation lastSeenAt(DateTime? lastSeenAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolDeviation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolDeviation(...).copyWith(id: 12, name: "My name")
  /// ```
  PolDeviation call({
    String subjectId,
    String? subjectRef,
    PolDeviationDeviationTypeEnum deviationType,
    DateTime at,
    String? placeId,
    String? placeLabel,
    PolDeviationStay? stay,
    int? hourOfWeek,
    num? bucketShare,
    DateTime? lastSeenAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolDeviation.copyWith(...)` or call `instanceOfPolDeviation.copyWith.fieldName(value)` for a single field.
class _$PolDeviationCWProxyImpl implements _$PolDeviationCWProxy {
  const _$PolDeviationCWProxyImpl(this._value);

  final PolDeviation _value;

  @override
  PolDeviation subjectId(String subjectId) => call(subjectId: subjectId);

  @override
  PolDeviation subjectRef(String? subjectRef) => call(subjectRef: subjectRef);

  @override
  PolDeviation deviationType(PolDeviationDeviationTypeEnum deviationType) =>
      call(deviationType: deviationType);

  @override
  PolDeviation at(DateTime at) => call(at: at);

  @override
  PolDeviation placeId(String? placeId) => call(placeId: placeId);

  @override
  PolDeviation placeLabel(String? placeLabel) => call(placeLabel: placeLabel);

  @override
  PolDeviation stay(PolDeviationStay? stay) => call(stay: stay);

  @override
  PolDeviation hourOfWeek(int? hourOfWeek) => call(hourOfWeek: hourOfWeek);

  @override
  PolDeviation bucketShare(num? bucketShare) => call(bucketShare: bucketShare);

  @override
  PolDeviation lastSeenAt(DateTime? lastSeenAt) => call(lastSeenAt: lastSeenAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolDeviation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolDeviation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolDeviation call({
    Object? subjectId = const $CopyWithPlaceholder(),
    Object? subjectRef = const $CopyWithPlaceholder(),
    Object? deviationType = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? placeLabel = const $CopyWithPlaceholder(),
    Object? stay = const $CopyWithPlaceholder(),
    Object? hourOfWeek = const $CopyWithPlaceholder(),
    Object? bucketShare = const $CopyWithPlaceholder(),
    Object? lastSeenAt = const $CopyWithPlaceholder(),
  }) {
    return PolDeviation(
      subjectId: subjectId == const $CopyWithPlaceholder() || subjectId == null
          ? _value.subjectId
          // ignore: cast_nullable_to_non_nullable
          : subjectId as String,
      subjectRef: subjectRef == const $CopyWithPlaceholder()
          ? _value.subjectRef
          // ignore: cast_nullable_to_non_nullable
          : subjectRef as String?,
      deviationType:
          deviationType == const $CopyWithPlaceholder() || deviationType == null
          ? _value.deviationType
          // ignore: cast_nullable_to_non_nullable
          : deviationType as PolDeviationDeviationTypeEnum,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      placeLabel: placeLabel == const $CopyWithPlaceholder()
          ? _value.placeLabel
          // ignore: cast_nullable_to_non_nullable
          : placeLabel as String?,
      stay: stay == const $CopyWithPlaceholder()
          ? _value.stay
          // ignore: cast_nullable_to_non_nullable
          : stay as PolDeviationStay?,
      hourOfWeek: hourOfWeek == const $CopyWithPlaceholder()
          ? _value.hourOfWeek
          // ignore: cast_nullable_to_non_nullable
          : hourOfWeek as int?,
      bucketShare: bucketShare == const $CopyWithPlaceholder()
          ? _value.bucketShare
          // ignore: cast_nullable_to_non_nullable
          : bucketShare as num?,
      lastSeenAt: lastSeenAt == const $CopyWithPlaceholder()
          ? _value.lastSeenAt
          // ignore: cast_nullable_to_non_nullable
          : lastSeenAt as DateTime?,
    );
  }
}

extension $PolDeviationCopyWith on PolDeviation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolDeviation.copyWith(...)` or `instanceOfPolDeviation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolDeviationCWProxy get copyWith => _$PolDeviationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolDeviation _$PolDeviationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PolDeviation', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['subjectId', 'deviationType', 'at'],
      );
      final val = PolDeviation(
        subjectId: $checkedConvert('subjectId', (v) => v as String),
        subjectRef: $checkedConvert('subjectRef', (v) => v as String?),
        deviationType: $checkedConvert(
          'deviationType',
          (v) => $enumDecode(_$PolDeviationDeviationTypeEnumEnumMap, v),
        ),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
        placeId: $checkedConvert('placeId', (v) => v as String?),
        placeLabel: $checkedConvert('placeLabel', (v) => v as String?),
        stay: $checkedConvert(
          'stay',
          (v) => v == null
              ? null
              : PolDeviationStay.fromJson(v as Map<String, dynamic>),
        ),
        hourOfWeek: $checkedConvert('hourOfWeek', (v) => (v as num?)?.toInt()),
        bucketShare: $checkedConvert('bucketShare', (v) => v as num?),
        lastSeenAt: $checkedConvert(
          'lastSeenAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PolDeviationToJson(PolDeviation instance) =>
    <String, dynamic>{
      'subjectId': instance.subjectId,
      'subjectRef': ?instance.subjectRef,
      'deviationType':
          _$PolDeviationDeviationTypeEnumEnumMap[instance.deviationType]!,
      'at': instance.at.toIso8601String(),
      'placeId': ?instance.placeId,
      'placeLabel': ?instance.placeLabel,
      'stay': ?instance.stay?.toJson(),
      'hourOfWeek': ?instance.hourOfWeek,
      'bucketShare': ?instance.bucketShare,
      'lastSeenAt': ?instance.lastSeenAt?.toIso8601String(),
    };

const _$PolDeviationDeviationTypeEnumEnumMap = {
  PolDeviationDeviationTypeEnum.NEW_PLACE: 'NEW_PLACE',
  PolDeviationDeviationTypeEnum.UNUSUAL_TIME: 'UNUSUAL_TIME',
  PolDeviationDeviationTypeEnum.EXPECTED_ABSENT: 'EXPECTED_ABSENT',
};
