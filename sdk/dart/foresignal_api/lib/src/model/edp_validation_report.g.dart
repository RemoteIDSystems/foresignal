// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_validation_report.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpValidationReportCWProxy {
  EdpValidationReport manifestValid(bool? manifestValid);

  EdpValidationReport manifestErrors(
    List<EdpItemRejectionSchemaErrorsInner>? manifestErrors,
  );

  EdpValidationReport breakingChanges(List<String>? breakingChanges);

  EdpValidationReport itemsValid(int? itemsValid);

  EdpValidationReport rejected(List<EdpItemRejection>? rejected);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpValidationReport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpValidationReport(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpValidationReport call({
    bool? manifestValid,
    List<EdpItemRejectionSchemaErrorsInner>? manifestErrors,
    List<String>? breakingChanges,
    int? itemsValid,
    List<EdpItemRejection>? rejected,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpValidationReport.copyWith(...)` or call `instanceOfEdpValidationReport.copyWith.fieldName(value)` for a single field.
class _$EdpValidationReportCWProxyImpl implements _$EdpValidationReportCWProxy {
  const _$EdpValidationReportCWProxyImpl(this._value);

  final EdpValidationReport _value;

  @override
  EdpValidationReport manifestValid(bool? manifestValid) =>
      call(manifestValid: manifestValid);

  @override
  EdpValidationReport manifestErrors(
    List<EdpItemRejectionSchemaErrorsInner>? manifestErrors,
  ) => call(manifestErrors: manifestErrors);

  @override
  EdpValidationReport breakingChanges(List<String>? breakingChanges) =>
      call(breakingChanges: breakingChanges);

  @override
  EdpValidationReport itemsValid(int? itemsValid) =>
      call(itemsValid: itemsValid);

  @override
  EdpValidationReport rejected(List<EdpItemRejection>? rejected) =>
      call(rejected: rejected);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpValidationReport(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpValidationReport(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpValidationReport call({
    Object? manifestValid = const $CopyWithPlaceholder(),
    Object? manifestErrors = const $CopyWithPlaceholder(),
    Object? breakingChanges = const $CopyWithPlaceholder(),
    Object? itemsValid = const $CopyWithPlaceholder(),
    Object? rejected = const $CopyWithPlaceholder(),
  }) {
    return EdpValidationReport(
      manifestValid: manifestValid == const $CopyWithPlaceholder()
          ? _value.manifestValid
          // ignore: cast_nullable_to_non_nullable
          : manifestValid as bool?,
      manifestErrors: manifestErrors == const $CopyWithPlaceholder()
          ? _value.manifestErrors
          // ignore: cast_nullable_to_non_nullable
          : manifestErrors as List<EdpItemRejectionSchemaErrorsInner>?,
      breakingChanges: breakingChanges == const $CopyWithPlaceholder()
          ? _value.breakingChanges
          // ignore: cast_nullable_to_non_nullable
          : breakingChanges as List<String>?,
      itemsValid: itemsValid == const $CopyWithPlaceholder()
          ? _value.itemsValid
          // ignore: cast_nullable_to_non_nullable
          : itemsValid as int?,
      rejected: rejected == const $CopyWithPlaceholder()
          ? _value.rejected
          // ignore: cast_nullable_to_non_nullable
          : rejected as List<EdpItemRejection>?,
    );
  }
}

extension $EdpValidationReportCopyWith on EdpValidationReport {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpValidationReport.copyWith(...)` or `instanceOfEdpValidationReport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpValidationReportCWProxy get copyWith =>
      _$EdpValidationReportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpValidationReport _$EdpValidationReportFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpValidationReport', json, ($checkedConvert) {
      final val = EdpValidationReport(
        manifestValid: $checkedConvert('manifestValid', (v) => v as bool?),
        manifestErrors: $checkedConvert(
          'manifestErrors',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => EdpItemRejectionSchemaErrorsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        breakingChanges: $checkedConvert(
          'breakingChanges',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        itemsValid: $checkedConvert('itemsValid', (v) => (v as num?)?.toInt()),
        rejected: $checkedConvert(
          'rejected',
          (v) => (v as List<dynamic>?)
              ?.map((e) => EdpItemRejection.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpValidationReportToJson(
  EdpValidationReport instance,
) => <String, dynamic>{
  'manifestValid': ?instance.manifestValid,
  'manifestErrors': ?instance.manifestErrors?.map((e) => e.toJson()).toList(),
  'breakingChanges': ?instance.breakingChanges,
  'itemsValid': ?instance.itemsValid,
  'rejected': ?instance.rejected?.map((e) => e.toJson()).toList(),
};
