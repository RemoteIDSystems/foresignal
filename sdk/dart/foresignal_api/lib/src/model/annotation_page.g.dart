// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnnotationPageCWProxy {
  AnnotationPage items(List<Annotation> items);

  AnnotationPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AnnotationPage call({List<Annotation> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnnotationPage.copyWith(...)` or call `instanceOfAnnotationPage.copyWith.fieldName(value)` for a single field.
class _$AnnotationPageCWProxyImpl implements _$AnnotationPageCWProxy {
  const _$AnnotationPageCWProxyImpl(this._value);

  final AnnotationPage _value;

  @override
  AnnotationPage items(List<Annotation> items) => call(items: items);

  @override
  AnnotationPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnnotationPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnnotationPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnnotationPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AnnotationPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Annotation>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AnnotationPageCopyWith on AnnotationPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnnotationPage.copyWith(...)` or `instanceOfAnnotationPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnnotationPageCWProxy get copyWith => _$AnnotationPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnnotationPage _$AnnotationPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnnotationPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AnnotationPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Annotation.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AnnotationPageToJson(AnnotationPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
