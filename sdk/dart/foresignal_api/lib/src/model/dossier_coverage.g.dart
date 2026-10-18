// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dossier_coverage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DossierCoverageCWProxy {
  DossierCoverage status(DossierCoverageStatusEnum status);

  DossierCoverage stats(CoverageStats? stats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  DossierCoverage call({
    DossierCoverageStatusEnum status,
    CoverageStats? stats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDossierCoverage.copyWith(...)` or call `instanceOfDossierCoverage.copyWith.fieldName(value)` for a single field.
class _$DossierCoverageCWProxyImpl implements _$DossierCoverageCWProxy {
  const _$DossierCoverageCWProxyImpl(this._value);

  final DossierCoverage _value;

  @override
  DossierCoverage status(DossierCoverageStatusEnum status) =>
      call(status: status);

  @override
  DossierCoverage stats(CoverageStats? stats) => call(stats: stats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DossierCoverage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DossierCoverage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DossierCoverage call({
    Object? status = const $CopyWithPlaceholder(),
    Object? stats = const $CopyWithPlaceholder(),
  }) {
    return DossierCoverage(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as DossierCoverageStatusEnum,
      stats: stats == const $CopyWithPlaceholder()
          ? _value.stats
          // ignore: cast_nullable_to_non_nullable
          : stats as CoverageStats?,
    );
  }
}

extension $DossierCoverageCopyWith on DossierCoverage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDossierCoverage.copyWith(...)` or `instanceOfDossierCoverage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DossierCoverageCWProxy get copyWith => _$DossierCoverageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DossierCoverage _$DossierCoverageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DossierCoverage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = DossierCoverage(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$DossierCoverageStatusEnumEnumMap, v),
        ),
        stats: $checkedConvert(
          'stats',
          (v) => v == null
              ? null
              : CoverageStats.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DossierCoverageToJson(DossierCoverage instance) =>
    <String, dynamic>{
      'status': _$DossierCoverageStatusEnumEnumMap[instance.status]!,
      'stats': ?instance.stats?.toJson(),
    };

const _$DossierCoverageStatusEnumEnumMap = {
  DossierCoverageStatusEnum.ok: 'ok',
  DossierCoverageStatusEnum.partial: 'partial',
  DossierCoverageStatusEnum.unavailable: 'unavailable',
};
