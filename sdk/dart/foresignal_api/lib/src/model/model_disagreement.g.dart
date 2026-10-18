// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'model_disagreement.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ModelDisagreementCWProxy {
  ModelDisagreement fields(List<ModelDisagreementFieldsEnum> fields);

  ModelDisagreement spread(Map<String, num> spread);

  ModelDisagreement models(List<String> models);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ModelDisagreement(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ModelDisagreement(...).copyWith(id: 12, name: "My name")
  /// ```
  ModelDisagreement call({
    List<ModelDisagreementFieldsEnum> fields,
    Map<String, num> spread,
    List<String> models,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfModelDisagreement.copyWith(...)` or call `instanceOfModelDisagreement.copyWith.fieldName(value)` for a single field.
class _$ModelDisagreementCWProxyImpl implements _$ModelDisagreementCWProxy {
  const _$ModelDisagreementCWProxyImpl(this._value);

  final ModelDisagreement _value;

  @override
  ModelDisagreement fields(List<ModelDisagreementFieldsEnum> fields) =>
      call(fields: fields);

  @override
  ModelDisagreement spread(Map<String, num> spread) => call(spread: spread);

  @override
  ModelDisagreement models(List<String> models) => call(models: models);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ModelDisagreement(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ModelDisagreement(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ModelDisagreement call({
    Object? fields = const $CopyWithPlaceholder(),
    Object? spread = const $CopyWithPlaceholder(),
    Object? models = const $CopyWithPlaceholder(),
  }) {
    return ModelDisagreement(
      fields: fields == const $CopyWithPlaceholder() || fields == null
          ? _value.fields
          // ignore: cast_nullable_to_non_nullable
          : fields as List<ModelDisagreementFieldsEnum>,
      spread: spread == const $CopyWithPlaceholder() || spread == null
          ? _value.spread
          // ignore: cast_nullable_to_non_nullable
          : spread as Map<String, num>,
      models: models == const $CopyWithPlaceholder() || models == null
          ? _value.models
          // ignore: cast_nullable_to_non_nullable
          : models as List<String>,
    );
  }
}

extension $ModelDisagreementCopyWith on ModelDisagreement {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfModelDisagreement.copyWith(...)` or `instanceOfModelDisagreement.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ModelDisagreementCWProxy get copyWith =>
      _$ModelDisagreementCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ModelDisagreement _$ModelDisagreementFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ModelDisagreement', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['fields', 'spread', 'models']);
      final val = ModelDisagreement(
        fields: $checkedConvert(
          'fields',
          (v) => (v as List<dynamic>)
              .map((e) => $enumDecode(_$ModelDisagreementFieldsEnumEnumMap, e))
              .toList(),
        ),
        spread: $checkedConvert(
          'spread',
          (v) => Map<String, num>.from(v as Map),
        ),
        models: $checkedConvert(
          'models',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ModelDisagreementToJson(ModelDisagreement instance) =>
    <String, dynamic>{
      'fields': instance.fields
          .map((e) => _$ModelDisagreementFieldsEnumEnumMap[e]!)
          .toList(),
      'spread': instance.spread,
      'models': instance.models,
    };

const _$ModelDisagreementFieldsEnumEnumMap = {
  ModelDisagreementFieldsEnum.gust: 'gust',
  ModelDisagreementFieldsEnum.precip24h: 'precip24h',
  ModelDisagreementFieldsEnum.t2mMax: 't2mMax',
};
