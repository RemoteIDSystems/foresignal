// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'deception_payload_all_of_checks.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DeceptionPayloadAllOfChecksCWProxy {
  DeceptionPayloadAllOfChecks check(DeceptionPayloadAllOfChecksCheckEnum check);

  DeceptionPayloadAllOfChecks count(int count);

  DeceptionPayloadAllOfChecks lastAt(DateTime? lastAt);

  DeceptionPayloadAllOfChecks detail(String? detail);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeceptionPayloadAllOfChecks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeceptionPayloadAllOfChecks(...).copyWith(id: 12, name: "My name")
  /// ```
  DeceptionPayloadAllOfChecks call({
    DeceptionPayloadAllOfChecksCheckEnum check,
    int count,
    DateTime? lastAt,
    String? detail,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDeceptionPayloadAllOfChecks.copyWith(...)` or call `instanceOfDeceptionPayloadAllOfChecks.copyWith.fieldName(value)` for a single field.
class _$DeceptionPayloadAllOfChecksCWProxyImpl
    implements _$DeceptionPayloadAllOfChecksCWProxy {
  const _$DeceptionPayloadAllOfChecksCWProxyImpl(this._value);

  final DeceptionPayloadAllOfChecks _value;

  @override
  DeceptionPayloadAllOfChecks check(
    DeceptionPayloadAllOfChecksCheckEnum check,
  ) => call(check: check);

  @override
  DeceptionPayloadAllOfChecks count(int count) => call(count: count);

  @override
  DeceptionPayloadAllOfChecks lastAt(DateTime? lastAt) => call(lastAt: lastAt);

  @override
  DeceptionPayloadAllOfChecks detail(String? detail) => call(detail: detail);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeceptionPayloadAllOfChecks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeceptionPayloadAllOfChecks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DeceptionPayloadAllOfChecks call({
    Object? check = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? lastAt = const $CopyWithPlaceholder(),
    Object? detail = const $CopyWithPlaceholder(),
  }) {
    return DeceptionPayloadAllOfChecks(
      check: check == const $CopyWithPlaceholder() || check == null
          ? _value.check
          // ignore: cast_nullable_to_non_nullable
          : check as DeceptionPayloadAllOfChecksCheckEnum,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
      lastAt: lastAt == const $CopyWithPlaceholder()
          ? _value.lastAt
          // ignore: cast_nullable_to_non_nullable
          : lastAt as DateTime?,
      detail: detail == const $CopyWithPlaceholder()
          ? _value.detail
          // ignore: cast_nullable_to_non_nullable
          : detail as String?,
    );
  }
}

extension $DeceptionPayloadAllOfChecksCopyWith on DeceptionPayloadAllOfChecks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDeceptionPayloadAllOfChecks.copyWith(...)` or `instanceOfDeceptionPayloadAllOfChecks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DeceptionPayloadAllOfChecksCWProxy get copyWith =>
      _$DeceptionPayloadAllOfChecksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DeceptionPayloadAllOfChecks _$DeceptionPayloadAllOfChecksFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DeceptionPayloadAllOfChecks', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['check', 'count']);
  final val = DeceptionPayloadAllOfChecks(
    check: $checkedConvert(
      'check',
      (v) => $enumDecode(_$DeceptionPayloadAllOfChecksCheckEnumEnumMap, v),
    ),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
    lastAt: $checkedConvert(
      'lastAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    detail: $checkedConvert('detail', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$DeceptionPayloadAllOfChecksToJson(
  DeceptionPayloadAllOfChecks instance,
) => <String, dynamic>{
  'check': _$DeceptionPayloadAllOfChecksCheckEnumEnumMap[instance.check]!,
  'count': instance.count,
  'lastAt': ?instance.lastAt?.toIso8601String(),
  'detail': ?instance.detail,
};

const _$DeceptionPayloadAllOfChecksCheckEnumEnumMap = {
  DeceptionPayloadAllOfChecksCheckEnum.duplicateMmsi: 'duplicate-mmsi',
  DeceptionPayloadAllOfChecksCheckEnum.vesselImpliedSpeed:
      'vessel-implied-speed',
  DeceptionPayloadAllOfChecksCheckEnum.aircraftImpliedSpeed:
      'aircraft-implied-speed',
};
