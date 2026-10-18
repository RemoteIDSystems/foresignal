// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'near_one_of2_anchor.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NearOneOf2AnchorCWProxy {
  NearOneOf2Anchor source_(String source_);

  NearOneOf2Anchor externalId(String externalId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NearOneOf2Anchor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NearOneOf2Anchor(...).copyWith(id: 12, name: "My name")
  /// ```
  NearOneOf2Anchor call({String source_, String externalId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNearOneOf2Anchor.copyWith(...)` or call `instanceOfNearOneOf2Anchor.copyWith.fieldName(value)` for a single field.
class _$NearOneOf2AnchorCWProxyImpl implements _$NearOneOf2AnchorCWProxy {
  const _$NearOneOf2AnchorCWProxyImpl(this._value);

  final NearOneOf2Anchor _value;

  @override
  NearOneOf2Anchor source_(String source_) => call(source_: source_);

  @override
  NearOneOf2Anchor externalId(String externalId) =>
      call(externalId: externalId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NearOneOf2Anchor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NearOneOf2Anchor(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NearOneOf2Anchor call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
  }) {
    return NearOneOf2Anchor(
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
    );
  }
}

extension $NearOneOf2AnchorCopyWith on NearOneOf2Anchor {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNearOneOf2Anchor.copyWith(...)` or `instanceOfNearOneOf2Anchor.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NearOneOf2AnchorCWProxy get copyWith => _$NearOneOf2AnchorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NearOneOf2Anchor _$NearOneOf2AnchorFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NearOneOf2Anchor', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['source', 'externalId']);
      final val = NearOneOf2Anchor(
        source_: $checkedConvert('source', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$NearOneOf2AnchorToJson(NearOneOf2Anchor instance) =>
    <String, dynamic>{
      'source': instance.source_,
      'externalId': instance.externalId,
    };
