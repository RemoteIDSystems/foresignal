// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_facilities.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposureFacilitiesCWProxy {
  ExposureFacilities status(ExposureFacilitiesStatusEnum status);

  ExposureFacilities attribution(List<Attribution>? attribution);

  ExposureFacilities counts(Map<String, int>? counts);

  ExposureFacilities total(int? total);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureFacilities(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureFacilities(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposureFacilities call({
    ExposureFacilitiesStatusEnum status,
    List<Attribution>? attribution,
    Map<String, int>? counts,
    int? total,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposureFacilities.copyWith(...)` or call `instanceOfExposureFacilities.copyWith.fieldName(value)` for a single field.
class _$ExposureFacilitiesCWProxyImpl implements _$ExposureFacilitiesCWProxy {
  const _$ExposureFacilitiesCWProxyImpl(this._value);

  final ExposureFacilities _value;

  @override
  ExposureFacilities status(ExposureFacilitiesStatusEnum status) =>
      call(status: status);

  @override
  ExposureFacilities attribution(List<Attribution>? attribution) =>
      call(attribution: attribution);

  @override
  ExposureFacilities counts(Map<String, int>? counts) => call(counts: counts);

  @override
  ExposureFacilities total(int? total) => call(total: total);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureFacilities(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureFacilities(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposureFacilities call({
    Object? status = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? counts = const $CopyWithPlaceholder(),
    Object? total = const $CopyWithPlaceholder(),
  }) {
    return ExposureFacilities(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ExposureFacilitiesStatusEnum,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<Attribution>?,
      counts: counts == const $CopyWithPlaceholder()
          ? _value.counts
          // ignore: cast_nullable_to_non_nullable
          : counts as Map<String, int>?,
      total: total == const $CopyWithPlaceholder()
          ? _value.total
          // ignore: cast_nullable_to_non_nullable
          : total as int?,
    );
  }
}

extension $ExposureFacilitiesCopyWith on ExposureFacilities {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposureFacilities.copyWith(...)` or `instanceOfExposureFacilities.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposureFacilitiesCWProxy get copyWith =>
      _$ExposureFacilitiesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposureFacilities _$ExposureFacilitiesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExposureFacilities', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = ExposureFacilities(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$ExposureFacilitiesStatusEnumEnumMap, v),
        ),
        attribution: $checkedConvert(
          'attribution',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        counts: $checkedConvert(
          'counts',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
        total: $checkedConvert('total', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ExposureFacilitiesToJson(ExposureFacilities instance) =>
    <String, dynamic>{
      'status': _$ExposureFacilitiesStatusEnumEnumMap[instance.status]!,
      'attribution': ?instance.attribution?.map((e) => e.toJson()).toList(),
      'counts': ?instance.counts,
      'total': ?instance.total,
    };

const _$ExposureFacilitiesStatusEnumEnumMap = {
  ExposureFacilitiesStatusEnum.ok: 'ok',
  ExposureFacilitiesStatusEnum.unavailable: 'unavailable',
  ExposureFacilitiesStatusEnum.notInstalled: 'not-installed',
};
