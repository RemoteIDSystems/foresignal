// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_dependents.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposureDependentsCWProxy {
  ExposureDependents status(ExposureDependentsStatusEnum status);

  ExposureDependents attribution(List<Attribution>? attribution);

  ExposureDependents buildId(String? buildId);

  ExposureDependents byLabel(ExposureDependentsByLabel? byLabel);

  ExposureDependents byType(Map<String, int>? byType);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureDependents(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureDependents(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposureDependents call({
    ExposureDependentsStatusEnum status,
    List<Attribution>? attribution,
    String? buildId,
    ExposureDependentsByLabel? byLabel,
    Map<String, int>? byType,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposureDependents.copyWith(...)` or call `instanceOfExposureDependents.copyWith.fieldName(value)` for a single field.
class _$ExposureDependentsCWProxyImpl implements _$ExposureDependentsCWProxy {
  const _$ExposureDependentsCWProxyImpl(this._value);

  final ExposureDependents _value;

  @override
  ExposureDependents status(ExposureDependentsStatusEnum status) =>
      call(status: status);

  @override
  ExposureDependents attribution(List<Attribution>? attribution) =>
      call(attribution: attribution);

  @override
  ExposureDependents buildId(String? buildId) => call(buildId: buildId);

  @override
  ExposureDependents byLabel(ExposureDependentsByLabel? byLabel) =>
      call(byLabel: byLabel);

  @override
  ExposureDependents byType(Map<String, int>? byType) => call(byType: byType);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureDependents(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureDependents(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposureDependents call({
    Object? status = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? buildId = const $CopyWithPlaceholder(),
    Object? byLabel = const $CopyWithPlaceholder(),
    Object? byType = const $CopyWithPlaceholder(),
  }) {
    return ExposureDependents(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ExposureDependentsStatusEnum,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<Attribution>?,
      buildId: buildId == const $CopyWithPlaceholder()
          ? _value.buildId
          // ignore: cast_nullable_to_non_nullable
          : buildId as String?,
      byLabel: byLabel == const $CopyWithPlaceholder()
          ? _value.byLabel
          // ignore: cast_nullable_to_non_nullable
          : byLabel as ExposureDependentsByLabel?,
      byType: byType == const $CopyWithPlaceholder()
          ? _value.byType
          // ignore: cast_nullable_to_non_nullable
          : byType as Map<String, int>?,
    );
  }
}

extension $ExposureDependentsCopyWith on ExposureDependents {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposureDependents.copyWith(...)` or `instanceOfExposureDependents.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposureDependentsCWProxy get copyWith =>
      _$ExposureDependentsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposureDependents _$ExposureDependentsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExposureDependents', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = ExposureDependents(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$ExposureDependentsStatusEnumEnumMap, v),
        ),
        attribution: $checkedConvert(
          'attribution',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        buildId: $checkedConvert('buildId', (v) => v as String?),
        byLabel: $checkedConvert(
          'byLabel',
          (v) => v == null
              ? null
              : ExposureDependentsByLabel.fromJson(v as Map<String, dynamic>),
        ),
        byType: $checkedConvert(
          'byType',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ExposureDependentsToJson(ExposureDependents instance) =>
    <String, dynamic>{
      'status': _$ExposureDependentsStatusEnumEnumMap[instance.status]!,
      'attribution': ?instance.attribution?.map((e) => e.toJson()).toList(),
      'buildId': ?instance.buildId,
      'byLabel': ?instance.byLabel?.toJson(),
      'byType': ?instance.byType,
    };

const _$ExposureDependentsStatusEnumEnumMap = {
  ExposureDependentsStatusEnum.ok: 'ok',
  ExposureDependentsStatusEnum.unavailable: 'unavailable',
  ExposureDependentsStatusEnum.notInstalled: 'not-installed',
};
