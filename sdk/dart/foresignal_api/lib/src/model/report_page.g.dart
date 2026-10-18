// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportPageCWProxy {
  ReportPage items(List<Report> items);

  ReportPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportPage call({List<Report> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportPage.copyWith(...)` or call `instanceOfReportPage.copyWith.fieldName(value)` for a single field.
class _$ReportPageCWProxyImpl implements _$ReportPageCWProxy {
  const _$ReportPageCWProxyImpl(this._value);

  final ReportPage _value;

  @override
  ReportPage items(List<Report> items) => call(items: items);

  @override
  ReportPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ReportPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Report>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ReportPageCopyWith on ReportPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportPage.copyWith(...)` or `instanceOfReportPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportPageCWProxy get copyWith => _$ReportPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportPage _$ReportPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = ReportPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Report.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ReportPageToJson(ReportPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
