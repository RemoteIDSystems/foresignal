//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_imagery_items_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsImageryItemsInner {
  /// Returns a new [DossierSectionsImageryItemsInner] instance.
  DossierSectionsImageryItemsInner({

    required  this.ref,

    required  this.kind,

     this.title,

     this.time,

     this.sourceId,

     this.frameUrl,

     this.thumbnailUrl,

     this.bbox,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final DossierSectionsImageryItemsInnerKindEnum kind;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'time',
    required: false,
    includeIfNull: false,
  )


  final DateTime? time;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'frameUrl',
    required: false,
    includeIfNull: false,
  )


  final String? frameUrl;



  @JsonKey(
    
    name: r'thumbnailUrl',
    required: false,
    includeIfNull: false,
  )


  final String? thumbnailUrl;



  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsImageryItemsInner &&
      other.ref == ref &&
      other.kind == kind &&
      other.title == title &&
      other.time == time &&
      other.sourceId == sourceId &&
      other.frameUrl == frameUrl &&
      other.thumbnailUrl == thumbnailUrl &&
      other.bbox == bbox;

    @override
    int get hashCode =>
        ref.hashCode +
        kind.hashCode +
        title.hashCode +
        (time == null ? 0 : time.hashCode) +
        (sourceId == null ? 0 : sourceId.hashCode) +
        (frameUrl == null ? 0 : frameUrl.hashCode) +
        (thumbnailUrl == null ? 0 : thumbnailUrl.hashCode) +
        (bbox == null ? 0 : bbox.hashCode);

  factory DossierSectionsImageryItemsInner.fromJson(Map<String, dynamic> json) => _$DossierSectionsImageryItemsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsImageryItemsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DossierSectionsImageryItemsInnerKindEnum {
@JsonValue(r'raster-frame')
rasterFrame(r'raster-frame'),
@JsonValue(r'satdump-product')
satdumpProduct(r'satdump-product'),
@JsonValue(r'stac-scene')
stacScene(r'stac-scene');

const DossierSectionsImageryItemsInnerKindEnum(this.value);

final String value;

@override
String toString() => value;
}


