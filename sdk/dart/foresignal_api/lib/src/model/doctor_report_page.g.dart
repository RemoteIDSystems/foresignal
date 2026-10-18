// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'doctor_report_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DoctorReportPageCWProxy {
  DoctorReportPage items(List<DoctorReport> items);

  DoctorReportPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportPage(...).copyWith(id: 12, name: "My name")
  /// ```
  DoctorReportPage call({List<DoctorReport> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDoctorReportPage.copyWith(...)` or call `instanceOfDoctorReportPage.copyWith.fieldName(value)` for a single field.
class _$DoctorReportPageCWProxyImpl implements _$DoctorReportPageCWProxy {
  const _$DoctorReportPageCWProxyImpl(this._value);

  final DoctorReportPage _value;

  @override
  DoctorReportPage items(List<DoctorReport> items) => call(items: items);

  @override
  DoctorReportPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DoctorReportPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DoctorReportPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DoctorReportPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return DoctorReportPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<DoctorReport>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $DoctorReportPageCopyWith on DoctorReportPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDoctorReportPage.copyWith(...)` or `instanceOfDoctorReportPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DoctorReportPageCWProxy get copyWith => _$DoctorReportPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DoctorReportPage _$DoctorReportPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DoctorReportPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = DoctorReportPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => DoctorReport.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$DoctorReportPageToJson(DoctorReportPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
