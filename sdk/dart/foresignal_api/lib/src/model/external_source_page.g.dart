// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_source_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalSourcePageCWProxy {
  ExternalSourcePage items(List<ExternalSource> items);

  ExternalSourcePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSourcePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSourcePage(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalSourcePage call({List<ExternalSource> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalSourcePage.copyWith(...)` or call `instanceOfExternalSourcePage.copyWith.fieldName(value)` for a single field.
class _$ExternalSourcePageCWProxyImpl implements _$ExternalSourcePageCWProxy {
  const _$ExternalSourcePageCWProxyImpl(this._value);

  final ExternalSourcePage _value;

  @override
  ExternalSourcePage items(List<ExternalSource> items) => call(items: items);

  @override
  ExternalSourcePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSourcePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSourcePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalSourcePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ExternalSourcePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ExternalSource>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ExternalSourcePageCopyWith on ExternalSourcePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalSourcePage.copyWith(...)` or `instanceOfExternalSourcePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalSourcePageCWProxy get copyWith =>
      _$ExternalSourcePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalSourcePage _$ExternalSourcePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExternalSourcePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ExternalSourcePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ExternalSource.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ExternalSourcePageToJson(ExternalSourcePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
