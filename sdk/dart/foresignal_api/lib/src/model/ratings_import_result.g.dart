// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ratings_import_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RatingsImportResultCWProxy {
  RatingsImportResult provider(RatingsImportResultProviderEnum provider);

  RatingsImportResult imported(int imported);

  RatingsImportResult updated(int updated);

  RatingsImportResult rejected(int rejected);

  RatingsImportResult outletsCreated(int? outletsCreated);

  RatingsImportResult errors(List<RatingsImportResultErrorsInner>? errors);

  RatingsImportResult importedAt(DateTime importedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RatingsImportResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RatingsImportResult(...).copyWith(id: 12, name: "My name")
  /// ```
  RatingsImportResult call({
    RatingsImportResultProviderEnum provider,
    int imported,
    int updated,
    int rejected,
    int? outletsCreated,
    List<RatingsImportResultErrorsInner>? errors,
    DateTime importedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRatingsImportResult.copyWith(...)` or call `instanceOfRatingsImportResult.copyWith.fieldName(value)` for a single field.
class _$RatingsImportResultCWProxyImpl implements _$RatingsImportResultCWProxy {
  const _$RatingsImportResultCWProxyImpl(this._value);

  final RatingsImportResult _value;

  @override
  RatingsImportResult provider(RatingsImportResultProviderEnum provider) =>
      call(provider: provider);

  @override
  RatingsImportResult imported(int imported) => call(imported: imported);

  @override
  RatingsImportResult updated(int updated) => call(updated: updated);

  @override
  RatingsImportResult rejected(int rejected) => call(rejected: rejected);

  @override
  RatingsImportResult outletsCreated(int? outletsCreated) =>
      call(outletsCreated: outletsCreated);

  @override
  RatingsImportResult errors(List<RatingsImportResultErrorsInner>? errors) =>
      call(errors: errors);

  @override
  RatingsImportResult importedAt(DateTime importedAt) =>
      call(importedAt: importedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RatingsImportResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RatingsImportResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RatingsImportResult call({
    Object? provider = const $CopyWithPlaceholder(),
    Object? imported = const $CopyWithPlaceholder(),
    Object? updated = const $CopyWithPlaceholder(),
    Object? rejected = const $CopyWithPlaceholder(),
    Object? outletsCreated = const $CopyWithPlaceholder(),
    Object? errors = const $CopyWithPlaceholder(),
    Object? importedAt = const $CopyWithPlaceholder(),
  }) {
    return RatingsImportResult(
      provider: provider == const $CopyWithPlaceholder() || provider == null
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as RatingsImportResultProviderEnum,
      imported: imported == const $CopyWithPlaceholder() || imported == null
          ? _value.imported
          // ignore: cast_nullable_to_non_nullable
          : imported as int,
      updated: updated == const $CopyWithPlaceholder() || updated == null
          ? _value.updated
          // ignore: cast_nullable_to_non_nullable
          : updated as int,
      rejected: rejected == const $CopyWithPlaceholder() || rejected == null
          ? _value.rejected
          // ignore: cast_nullable_to_non_nullable
          : rejected as int,
      outletsCreated: outletsCreated == const $CopyWithPlaceholder()
          ? _value.outletsCreated
          // ignore: cast_nullable_to_non_nullable
          : outletsCreated as int?,
      errors: errors == const $CopyWithPlaceholder()
          ? _value.errors
          // ignore: cast_nullable_to_non_nullable
          : errors as List<RatingsImportResultErrorsInner>?,
      importedAt:
          importedAt == const $CopyWithPlaceholder() || importedAt == null
          ? _value.importedAt
          // ignore: cast_nullable_to_non_nullable
          : importedAt as DateTime,
    );
  }
}

extension $RatingsImportResultCopyWith on RatingsImportResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRatingsImportResult.copyWith(...)` or `instanceOfRatingsImportResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RatingsImportResultCWProxy get copyWith =>
      _$RatingsImportResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RatingsImportResult _$RatingsImportResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RatingsImportResult', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'provider',
          'imported',
          'updated',
          'rejected',
          'importedAt',
        ],
      );
      final val = RatingsImportResult(
        provider: $checkedConvert(
          'provider',
          (v) => $enumDecode(_$RatingsImportResultProviderEnumEnumMap, v),
        ),
        imported: $checkedConvert('imported', (v) => (v as num).toInt()),
        updated: $checkedConvert('updated', (v) => (v as num).toInt()),
        rejected: $checkedConvert('rejected', (v) => (v as num).toInt()),
        outletsCreated: $checkedConvert(
          'outletsCreated',
          (v) => (v as num?)?.toInt(),
        ),
        errors: $checkedConvert(
          'errors',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => RatingsImportResultErrorsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        importedAt: $checkedConvert(
          'importedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RatingsImportResultToJson(
  RatingsImportResult instance,
) => <String, dynamic>{
  'provider': _$RatingsImportResultProviderEnumEnumMap[instance.provider]!,
  'imported': instance.imported,
  'updated': instance.updated,
  'rejected': instance.rejected,
  'outletsCreated': ?instance.outletsCreated,
  'errors': ?instance.errors?.map((e) => e.toJson()).toList(),
  'importedAt': instance.importedAt.toIso8601String(),
};

const _$RatingsImportResultProviderEnumEnumMap = {
  RatingsImportResultProviderEnum.wikidata: 'wikidata',
  RatingsImportResultProviderEnum.partisanWeb: 'partisan-web',
  RatingsImportResultProviderEnum.allsides: 'allsides',
  RatingsImportResultProviderEnum.mbfc: 'mbfc',
  RatingsImportResultProviderEnum.operator_: 'operator',
};
