// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'export_job_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExportJobPageCWProxy {
  ExportJobPage items(List<ExportJob> items);

  ExportJobPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportJobPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportJobPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ExportJobPage call({List<ExportJob> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExportJobPage.copyWith(...)` or call `instanceOfExportJobPage.copyWith.fieldName(value)` for a single field.
class _$ExportJobPageCWProxyImpl implements _$ExportJobPageCWProxy {
  const _$ExportJobPageCWProxyImpl(this._value);

  final ExportJobPage _value;

  @override
  ExportJobPage items(List<ExportJob> items) => call(items: items);

  @override
  ExportJobPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportJobPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportJobPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExportJobPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ExportJobPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ExportJob>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ExportJobPageCopyWith on ExportJobPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExportJobPage.copyWith(...)` or `instanceOfExportJobPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExportJobPageCWProxy get copyWith => _$ExportJobPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExportJobPage _$ExportJobPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExportJobPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ExportJobPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ExportJob.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ExportJobPageToJson(ExportJobPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
