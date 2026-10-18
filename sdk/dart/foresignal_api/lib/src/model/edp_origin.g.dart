// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_origin.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpOriginCWProxy {
  EdpOrigin originator(String originator);

  EdpOrigin class_(EdpOriginClass_Enum? class_);

  EdpOrigin upstreamId(String? upstreamId);

  EdpOrigin derivedFrom(List<String>? derivedFrom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpOrigin(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpOrigin(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpOrigin call({
    String originator,
    EdpOriginClass_Enum? class_,
    String? upstreamId,
    List<String>? derivedFrom,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpOrigin.copyWith(...)` or call `instanceOfEdpOrigin.copyWith.fieldName(value)` for a single field.
class _$EdpOriginCWProxyImpl implements _$EdpOriginCWProxy {
  const _$EdpOriginCWProxyImpl(this._value);

  final EdpOrigin _value;

  @override
  EdpOrigin originator(String originator) => call(originator: originator);

  @override
  EdpOrigin class_(EdpOriginClass_Enum? class_) => call(class_: class_);

  @override
  EdpOrigin upstreamId(String? upstreamId) => call(upstreamId: upstreamId);

  @override
  EdpOrigin derivedFrom(List<String>? derivedFrom) =>
      call(derivedFrom: derivedFrom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpOrigin(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpOrigin(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpOrigin call({
    Object? originator = const $CopyWithPlaceholder(),
    Object? class_ = const $CopyWithPlaceholder(),
    Object? upstreamId = const $CopyWithPlaceholder(),
    Object? derivedFrom = const $CopyWithPlaceholder(),
  }) {
    return EdpOrigin(
      originator:
          originator == const $CopyWithPlaceholder() || originator == null
          ? _value.originator
          // ignore: cast_nullable_to_non_nullable
          : originator as String,
      class_: class_ == const $CopyWithPlaceholder()
          ? _value.class_
          // ignore: cast_nullable_to_non_nullable
          : class_ as EdpOriginClass_Enum?,
      upstreamId: upstreamId == const $CopyWithPlaceholder()
          ? _value.upstreamId
          // ignore: cast_nullable_to_non_nullable
          : upstreamId as String?,
      derivedFrom: derivedFrom == const $CopyWithPlaceholder()
          ? _value.derivedFrom
          // ignore: cast_nullable_to_non_nullable
          : derivedFrom as List<String>?,
    );
  }
}

extension $EdpOriginCopyWith on EdpOrigin {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpOrigin.copyWith(...)` or `instanceOfEdpOrigin.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpOriginCWProxy get copyWith => _$EdpOriginCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpOrigin _$EdpOriginFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpOrigin', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['originator']);
      final val = EdpOrigin(
        originator: $checkedConvert('originator', (v) => v as String),
        class_: $checkedConvert(
          'class',
          (v) => $enumDecodeNullable(_$EdpOriginClass_EnumEnumMap, v),
        ),
        upstreamId: $checkedConvert('upstreamId', (v) => v as String?),
        derivedFrom: $checkedConvert(
          'derivedFrom',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    }, fieldKeyMap: const {'class_': 'class'});

Map<String, dynamic> _$EdpOriginToJson(EdpOrigin instance) => <String, dynamic>{
  'originator': instance.originator,
  'class': ?_$EdpOriginClass_EnumEnumMap[instance.class_],
  'upstreamId': ?instance.upstreamId,
  'derivedFrom': ?instance.derivedFrom,
};

const _$EdpOriginClass_EnumEnumMap = {
  EdpOriginClass_Enum.sensorNetwork: 'sensor-network',
  EdpOriginClass_Enum.agency: 'agency',
  EdpOriginClass_Enum.aggregator: 'aggregator',
  EdpOriginClass_Enum.media: 'media',
  EdpOriginClass_Enum.crowd: 'crowd',
  EdpOriginClass_Enum.social: 'social',
  EdpOriginClass_Enum.operator_: 'operator',
  EdpOriginClass_Enum.derived: 'derived',
};
