// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_source_created.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalSourceCreatedCWProxy {
  ExternalSourceCreated source_(ExternalSource source_);

  ExternalSourceCreated ingestKey(String ingestKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSourceCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSourceCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalSourceCreated call({ExternalSource source_, String ingestKey});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalSourceCreated.copyWith(...)` or call `instanceOfExternalSourceCreated.copyWith.fieldName(value)` for a single field.
class _$ExternalSourceCreatedCWProxyImpl
    implements _$ExternalSourceCreatedCWProxy {
  const _$ExternalSourceCreatedCWProxyImpl(this._value);

  final ExternalSourceCreated _value;

  @override
  ExternalSourceCreated source_(ExternalSource source_) =>
      call(source_: source_);

  @override
  ExternalSourceCreated ingestKey(String ingestKey) =>
      call(ingestKey: ingestKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSourceCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSourceCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalSourceCreated call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? ingestKey = const $CopyWithPlaceholder(),
  }) {
    return ExternalSourceCreated(
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as ExternalSource,
      ingestKey: ingestKey == const $CopyWithPlaceholder() || ingestKey == null
          ? _value.ingestKey
          // ignore: cast_nullable_to_non_nullable
          : ingestKey as String,
    );
  }
}

extension $ExternalSourceCreatedCopyWith on ExternalSourceCreated {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalSourceCreated.copyWith(...)` or `instanceOfExternalSourceCreated.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalSourceCreatedCWProxy get copyWith =>
      _$ExternalSourceCreatedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalSourceCreated _$ExternalSourceCreatedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalSourceCreated', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['source', 'ingestKey']);
  final val = ExternalSourceCreated(
    source_: $checkedConvert(
      'source',
      (v) => ExternalSource.fromJson(v as Map<String, dynamic>),
    ),
    ingestKey: $checkedConvert('ingestKey', (v) => v as String),
  );
  return val;
}, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$ExternalSourceCreatedToJson(
  ExternalSourceCreated instance,
) => <String, dynamic>{
  'source': instance.source_.toJson(),
  'ingestKey': instance.ingestKey,
};
