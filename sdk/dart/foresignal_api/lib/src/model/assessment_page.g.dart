// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentPageCWProxy {
  AssessmentPage items(List<Assessment> items);

  AssessmentPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AssessmentPage call({List<Assessment> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessmentPage.copyWith(...)` or call `instanceOfAssessmentPage.copyWith.fieldName(value)` for a single field.
class _$AssessmentPageCWProxyImpl implements _$AssessmentPageCWProxy {
  const _$AssessmentPageCWProxyImpl(this._value);

  final AssessmentPage _value;

  @override
  AssessmentPage items(List<Assessment> items) => call(items: items);

  @override
  AssessmentPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AssessmentPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AssessmentPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Assessment>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AssessmentPageCopyWith on AssessmentPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessmentPage.copyWith(...)` or `instanceOfAssessmentPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentPageCWProxy get copyWith => _$AssessmentPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssessmentPage _$AssessmentPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AssessmentPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AssessmentPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Assessment.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AssessmentPageToJson(AssessmentPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
