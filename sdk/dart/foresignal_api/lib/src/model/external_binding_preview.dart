//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/external_binding_preview_effects_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_binding_preview.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalBindingPreview {
  /// Returns a new [ExternalBindingPreview] instance.
  ExternalBindingPreview({

    required  this.evaluated,

    required  this.matched,

    required  this.effects,

     this.errors,
  });

  @JsonKey(
    
    name: r'evaluated',
    required: true,
    includeIfNull: false,
  )


  final int evaluated;



  @JsonKey(
    
    name: r'matched',
    required: true,
    includeIfNull: false,
  )


  final int matched;



  @JsonKey(
    
    name: r'effects',
    required: true,
    includeIfNull: false,
  )


  final List<ExternalBindingPreviewEffectsInner> effects;



  @JsonKey(
    
    name: r'errors',
    required: false,
    includeIfNull: false,
  )


  final List<String>? errors;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalBindingPreview &&
      other.evaluated == evaluated &&
      other.matched == matched &&
      other.effects == effects &&
      other.errors == errors;

    @override
    int get hashCode =>
        evaluated.hashCode +
        matched.hashCode +
        effects.hashCode +
        errors.hashCode;

  factory ExternalBindingPreview.fromJson(Map<String, dynamic> json) => _$ExternalBindingPreviewFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalBindingPreviewToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

