// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'import_media_ratings_request1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ImportMediaRatingsRequest1CWProxy {
  ImportMediaRatingsRequest1 rows(
    List<ImportMediaRatingsRequest1RowsInner> rows,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImportMediaRatingsRequest1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImportMediaRatingsRequest1(...).copyWith(id: 12, name: "My name")
  /// ```
  ImportMediaRatingsRequest1 call({
    List<ImportMediaRatingsRequest1RowsInner> rows,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfImportMediaRatingsRequest1.copyWith(...)` or call `instanceOfImportMediaRatingsRequest1.copyWith.fieldName(value)` for a single field.
class _$ImportMediaRatingsRequest1CWProxyImpl
    implements _$ImportMediaRatingsRequest1CWProxy {
  const _$ImportMediaRatingsRequest1CWProxyImpl(this._value);

  final ImportMediaRatingsRequest1 _value;

  @override
  ImportMediaRatingsRequest1 rows(
    List<ImportMediaRatingsRequest1RowsInner> rows,
  ) => call(rows: rows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ImportMediaRatingsRequest1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ImportMediaRatingsRequest1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ImportMediaRatingsRequest1 call({
    Object? rows = const $CopyWithPlaceholder(),
  }) {
    return ImportMediaRatingsRequest1(
      rows: rows == const $CopyWithPlaceholder() || rows == null
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as List<ImportMediaRatingsRequest1RowsInner>,
    );
  }
}

extension $ImportMediaRatingsRequest1CopyWith on ImportMediaRatingsRequest1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfImportMediaRatingsRequest1.copyWith(...)` or `instanceOfImportMediaRatingsRequest1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ImportMediaRatingsRequest1CWProxy get copyWith =>
      _$ImportMediaRatingsRequest1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ImportMediaRatingsRequest1 _$ImportMediaRatingsRequest1FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ImportMediaRatingsRequest1', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['rows']);
  final val = ImportMediaRatingsRequest1(
    rows: $checkedConvert(
      'rows',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ImportMediaRatingsRequest1RowsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ImportMediaRatingsRequest1ToJson(
  ImportMediaRatingsRequest1 instance,
) => <String, dynamic>{'rows': instance.rows.map((e) => e.toJson()).toList()};
