// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'emwin_product_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EmwinProductPayloadCWProxy {
  EmwinProductPayload heading(String heading);

  EmwinProductPayload awipsId(String awipsId);

  EmwinProductPayload office(String? office);

  EmwinProductPayload issuedAt(DateTime issuedAt);

  EmwinProductPayload text(String text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EmwinProductPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EmwinProductPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  EmwinProductPayload call({
    String heading,
    String awipsId,
    String? office,
    DateTime issuedAt,
    String text,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEmwinProductPayload.copyWith(...)` or call `instanceOfEmwinProductPayload.copyWith.fieldName(value)` for a single field.
class _$EmwinProductPayloadCWProxyImpl implements _$EmwinProductPayloadCWProxy {
  const _$EmwinProductPayloadCWProxyImpl(this._value);

  final EmwinProductPayload _value;

  @override
  EmwinProductPayload heading(String heading) => call(heading: heading);

  @override
  EmwinProductPayload awipsId(String awipsId) => call(awipsId: awipsId);

  @override
  EmwinProductPayload office(String? office) => call(office: office);

  @override
  EmwinProductPayload issuedAt(DateTime issuedAt) => call(issuedAt: issuedAt);

  @override
  EmwinProductPayload text(String text) => call(text: text);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EmwinProductPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EmwinProductPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EmwinProductPayload call({
    Object? heading = const $CopyWithPlaceholder(),
    Object? awipsId = const $CopyWithPlaceholder(),
    Object? office = const $CopyWithPlaceholder(),
    Object? issuedAt = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
  }) {
    return EmwinProductPayload(
      heading: heading == const $CopyWithPlaceholder() || heading == null
          ? _value.heading
          // ignore: cast_nullable_to_non_nullable
          : heading as String,
      awipsId: awipsId == const $CopyWithPlaceholder() || awipsId == null
          ? _value.awipsId
          // ignore: cast_nullable_to_non_nullable
          : awipsId as String,
      office: office == const $CopyWithPlaceholder()
          ? _value.office
          // ignore: cast_nullable_to_non_nullable
          : office as String?,
      issuedAt: issuedAt == const $CopyWithPlaceholder() || issuedAt == null
          ? _value.issuedAt
          // ignore: cast_nullable_to_non_nullable
          : issuedAt as DateTime,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
    );
  }
}

extension $EmwinProductPayloadCopyWith on EmwinProductPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEmwinProductPayload.copyWith(...)` or `instanceOfEmwinProductPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EmwinProductPayloadCWProxy get copyWith =>
      _$EmwinProductPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EmwinProductPayload _$EmwinProductPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EmwinProductPayload', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['heading', 'awipsId', 'issuedAt', 'text'],
      );
      final val = EmwinProductPayload(
        heading: $checkedConvert('heading', (v) => v as String),
        awipsId: $checkedConvert('awipsId', (v) => v as String),
        office: $checkedConvert('office', (v) => v as String?),
        issuedAt: $checkedConvert(
          'issuedAt',
          (v) => DateTime.parse(v as String),
        ),
        text: $checkedConvert('text', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$EmwinProductPayloadToJson(
  EmwinProductPayload instance,
) => <String, dynamic>{
  'heading': instance.heading,
  'awipsId': instance.awipsId,
  'office': ?instance.office,
  'issuedAt': instance.issuedAt.toIso8601String(),
  'text': instance.text,
};
