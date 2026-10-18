// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_envelope_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectEnvelopePageCWProxy {
  ObjectEnvelopePage items(List<ObjectEnvelope> items);

  ObjectEnvelopePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectEnvelopePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectEnvelopePage(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectEnvelopePage call({List<ObjectEnvelope> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectEnvelopePage.copyWith(...)` or call `instanceOfObjectEnvelopePage.copyWith.fieldName(value)` for a single field.
class _$ObjectEnvelopePageCWProxyImpl implements _$ObjectEnvelopePageCWProxy {
  const _$ObjectEnvelopePageCWProxyImpl(this._value);

  final ObjectEnvelopePage _value;

  @override
  ObjectEnvelopePage items(List<ObjectEnvelope> items) => call(items: items);

  @override
  ObjectEnvelopePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectEnvelopePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectEnvelopePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectEnvelopePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ObjectEnvelopePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ObjectEnvelope>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ObjectEnvelopePageCopyWith on ObjectEnvelopePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectEnvelopePage.copyWith(...)` or `instanceOfObjectEnvelopePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectEnvelopePageCWProxy get copyWith =>
      _$ObjectEnvelopePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectEnvelopePage _$ObjectEnvelopePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ObjectEnvelopePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = ObjectEnvelopePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ObjectEnvelopePageToJson(ObjectEnvelopePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
