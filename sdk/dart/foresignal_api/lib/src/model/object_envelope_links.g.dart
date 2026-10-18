// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'object_envelope_links.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ObjectEnvelopeLinksCWProxy {
  ObjectEnvelopeLinks self(String? self);

  ObjectEnvelopeLinks related(String? related);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectEnvelopeLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectEnvelopeLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  ObjectEnvelopeLinks call({String? self, String? related});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfObjectEnvelopeLinks.copyWith(...)` or call `instanceOfObjectEnvelopeLinks.copyWith.fieldName(value)` for a single field.
class _$ObjectEnvelopeLinksCWProxyImpl implements _$ObjectEnvelopeLinksCWProxy {
  const _$ObjectEnvelopeLinksCWProxyImpl(this._value);

  final ObjectEnvelopeLinks _value;

  @override
  ObjectEnvelopeLinks self(String? self) => call(self: self);

  @override
  ObjectEnvelopeLinks related(String? related) => call(related: related);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ObjectEnvelopeLinks(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ObjectEnvelopeLinks(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ObjectEnvelopeLinks call({
    Object? self = const $CopyWithPlaceholder(),
    Object? related = const $CopyWithPlaceholder(),
  }) {
    return ObjectEnvelopeLinks(
      self: self == const $CopyWithPlaceholder()
          ? _value.self
          // ignore: cast_nullable_to_non_nullable
          : self as String?,
      related: related == const $CopyWithPlaceholder()
          ? _value.related
          // ignore: cast_nullable_to_non_nullable
          : related as String?,
    );
  }
}

extension $ObjectEnvelopeLinksCopyWith on ObjectEnvelopeLinks {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfObjectEnvelopeLinks.copyWith(...)` or `instanceOfObjectEnvelopeLinks.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ObjectEnvelopeLinksCWProxy get copyWith =>
      _$ObjectEnvelopeLinksCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ObjectEnvelopeLinks _$ObjectEnvelopeLinksFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ObjectEnvelopeLinks', json, ($checkedConvert) {
      final val = ObjectEnvelopeLinks(
        self: $checkedConvert('self', (v) => v as String?),
        related: $checkedConvert('related', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ObjectEnvelopeLinksToJson(
  ObjectEnvelopeLinks instance,
) => <String, dynamic>{'self': ?instance.self, 'related': ?instance.related};
