//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/system_rasters_glm.dart';
import 'package:foresignal_api/src/model/system_rasters_caches_value.dart';
import 'package:foresignal_api/src/model/system_rasters_render_queue.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_rasters.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemRasters {
  /// Returns a new [SystemRasters] instance.
  SystemRasters({

    required  this.profile,

     this.products,

     this.frames,

     this.storeBytes,

     this.mediaBytes,

     this.diskQuotaGb,

     this.diskQuotaPct,

     this.caches,

     this.renderQueue,

     this.renderBusyLastHour,

     this.mbDownloadedToday,

     this.glm,
  });

  @JsonKey(
    
    name: r'profile',
    required: true,
    includeIfNull: false,
  )


  final SystemRastersProfileEnum profile;



  @JsonKey(
    
    name: r'products',
    required: false,
    includeIfNull: false,
  )


  final int? products;



  @JsonKey(
    
    name: r'frames',
    required: false,
    includeIfNull: false,
  )


  final int? frames;



  @JsonKey(
    
    name: r'storeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? storeBytes;



      /// Media store bytes (043); count against the disk quota
  @JsonKey(
    
    name: r'mediaBytes',
    required: false,
    includeIfNull: false,
  )


  final int? mediaBytes;



  @JsonKey(
    
    name: r'diskQuotaGb',
    required: false,
    includeIfNull: false,
  )


  final num? diskQuotaGb;



      /// Frame store, media store and GLM archive against the quota; at ≥ 90 % `/v1/system` degrades
  @JsonKey(
    
    name: r'diskQuotaPct',
    required: false,
    includeIfNull: false,
  )


  final num? diskQuotaPct;



      /// Per-cache metrics (frame, tile, index)
  @JsonKey(
    
    name: r'caches',
    required: false,
    includeIfNull: false,
  )


  final Map<String, SystemRastersCachesValue>? caches;



  @JsonKey(
    
    name: r'renderQueue',
    required: false,
    includeIfNull: false,
  )


  final SystemRastersRenderQueue? renderQueue;



      /// RENDER_BUSY responses in the last hour
  @JsonKey(
    
    name: r'renderBusyLastHour',
    required: false,
    includeIfNull: false,
  )


  final int? renderBusyLastHour;



  @JsonKey(
    
    name: r'mbDownloadedToday',
    required: false,
    includeIfNull: false,
  )


  final num? mbDownloadedToday;



  @JsonKey(
    
    name: r'glm',
    required: false,
    includeIfNull: false,
  )


  final SystemRastersGlm? glm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemRasters &&
      other.profile == profile &&
      other.products == products &&
      other.frames == frames &&
      other.storeBytes == storeBytes &&
      other.mediaBytes == mediaBytes &&
      other.diskQuotaGb == diskQuotaGb &&
      other.diskQuotaPct == diskQuotaPct &&
      other.caches == caches &&
      other.renderQueue == renderQueue &&
      other.renderBusyLastHour == renderBusyLastHour &&
      other.mbDownloadedToday == mbDownloadedToday &&
      other.glm == glm;

    @override
    int get hashCode =>
        profile.hashCode +
        products.hashCode +
        frames.hashCode +
        storeBytes.hashCode +
        mediaBytes.hashCode +
        diskQuotaGb.hashCode +
        diskQuotaPct.hashCode +
        caches.hashCode +
        renderQueue.hashCode +
        renderBusyLastHour.hashCode +
        mbDownloadedToday.hashCode +
        glm.hashCode;

  factory SystemRasters.fromJson(Map<String, dynamic> json) => _$SystemRastersFromJson(json);

  Map<String, dynamic> toJson() => _$SystemRastersToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SystemRastersProfileEnum {
@JsonValue(r'small')
small(r'small'),
@JsonValue(r'large')
large(r'large');

const SystemRastersProfileEnum(this.value);

final String value;

@override
String toString() => value;
}


